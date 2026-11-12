# BlogSystem 部署指南

> 目标：把 Spring Boot 后端、Vue 前端、MySQL、Redis 部署到一台 Linux 服务器，由 nginx 统一对外提供 80/443 服务。
>
> 下面以 **Ubuntu 22.04 / 24.04** 为例；CentOS/RHEL 的差异写在对应步骤里。
>
> 如果希望用 Docker Compose 一键部署，见 **[docker/README.md](docker/README.md)**。

## 0. 架构与端口

```
浏览器
  │ 80/443
  ▼
nginx  ── 静态文件 /var/www/blogsystem（Vue dist）
  │
  │ 反向代理 /user /article /category /comment /like /follow /upload
  ▼
Spring Boot 127.0.0.1:8080
  ├── MySQL 127.0.0.1:3306  数据库 blogsystem
  ├── Redis 127.0.0.1:6379  登录 token、验证码、点赞/关注缓存
  └── 阿里云 OSS            文章图片
```

- 对外只开放 **22 / 80 / 443**；8080、3306、6379 不要暴露到公网。
- 最低配置建议 2 核 2G、20G 磁盘。
- 项目需要 **JDK 11 或 17**（推荐 17）、MySQL 8、Redis 6+、nginx。

## 1. 安装基础环境

```bash
sudo apt update
sudo apt install -y openjdk-17-jre-headless mysql-server redis-server nginx maven unzip
java -version     # 应为 17.x
mvn -v            # 注意输出的 Java version 也应为 17
```

CentOS/RHEL：

```bash
sudo dnf install -y java-17-openjdk-headless mysql-server redis nginx maven
```

> 项目 `pom.xml` 指定 Java 11，JDK 17 可以正常编译运行。
> 如果服务器上同时有多个 JDK，用 `update-alternatives --config java` 切换到 17。

## 2. 准备数据库

### 2.1 从本机导出（在 Windows 当前机器执行）

项目仓库里没有 SQL 文件，需要从你本机已有的 `blogsystem` 数据库导出。
本机 MySQL 客户端路径是 `C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe`：

```powershell
& 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe' `
  --user=root --password `
  --single-transaction --default-character-set=utf8mb4 --set-gtid-purged=OFF `
  --routines --triggers `
  --result-file="D:\blogsystem.sql" blogsystem
```

> 上面的数据库名按实际为准（本机为 `blogsystem`）。执行后会提示输入 MySQL 密码。
> 使用 `--result-file` 而不是 PowerShell 的 `>`，可以避免中文编码被改写。

上传到服务器：

```powershell
scp D:\blogsystem.sql 用户名@服务器IP:/tmp/
```

### 2.2 在服务器创建库和用户

```bash
sudo mysql
```

```sql
CREATE DATABASE blogsystem
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'bloguser'@'127.0.0.1' IDENTIFIED BY '替换成强密码';
CREATE USER 'bloguser'@'localhost' IDENTIFIED BY '替换成强密码';
GRANT ALL PRIVILEGES ON blogsystem.* TO 'bloguser'@'127.0.0.1';
GRANT ALL PRIVILEGES ON blogsystem.* TO 'bloguser'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

导入：

```bash
mysql -u bloguser -p blogsystem < /tmp/blogsystem.sql
mysql -u bloguser -p -e "SHOW TABLES;" blogsystem
```

应能看到 `tb_article`、`tb_category`、`tb_comment`、`tb_follow`、`tb_user`。

> MySQL 8 默认使用 `caching_sha2_password`，项目里的 Connector/J 8.0.33 配合
> `allowPublicKeyRetrieval=true` 可以正常连接。
> 保持 MySQL 只监听 `127.0.0.1`，不要把 3306 开到公网。

## 3. 配置 Redis

```bash
sudo systemctl enable --now redis-server
redis-cli ping       # 返回 PONG
```

Redis 保存登录 token、验证码、点赞/关注集合，**必须运行**。

- 默认监听 `127.0.0.1:6379`，无需改配置。
- 如果设置了 `requirepass`，在 `application-prod.yaml` 中打开 `spring.redis.password`。
- Redis 重启后 token 可能丢失，用户重新登录即可。

## 4. 配置阿里云 OSS

项目图片上传走阿里云 OSS，Bucket 默认是 `blogsystems`，地域深圳。

1. 在阿里云控制台创建/使用 Bucket，记下 **Endpoint**、**Bucket 名**。
2. 创建有该 Bucket 读写权限的 AccessKey。
3. **Bucket 读权限设为「公共读」**，否则接口返回的 `https://bucket.endpoint/key` 图片会 403。
4. 把 AccessKey 填入服务器的 `/opt/blogsystem/blog.env`（下一步）。

> `application.yaml` 里写的是 `${OSS_ACCESS_KEY_ID}` / `${OSS_ACCESS_KEY_SECRET}`，
> 环境变量缺失时应用会在启动阶段报“无法解析占位符”，所以这两个值必填。

## 5. 部署后端

### 5.1 拉取代码并构建

仓库是私有的，服务器上 clone 时用户名填 `langjinghai`，密码处填 **GitHub PAT**（不是登录密码）：

```bash
cd ~
git clone https://github.com/langjinghai/blogsystem.git
cd blogsystem/blogSystem
mvn clean package -DskipTests
ls -lh target/blogSystem-0.0.1-SNAPSHOT.jar
```

> 如果服务器内存较小，构建时可加 `MAVEN_OPTS="-Xmx512m"`；也可以在本地用 IntelliJ 的 Maven 打包后 `scp` 上传 jar。
> 国内服务器如果 Maven 依赖下载慢，可在 `~/.m2/settings.xml` 配置阿里云镜像 `https://maven.aliyun.com/repository/public`。

### 5.2 安装为 systemd 服务

```bash
sudo useradd --system --home /opt/blogsystem --shell /usr/sbin/nologin blogsystem
sudo mkdir -p /opt/blogsystem

sudo cp target/blogSystem-0.0.1-SNAPSHOT.jar /opt/blogsystem/
sudo cp ~/blogsystem/deploy/application-prod.yaml.example /opt/blogsystem/application-prod.yaml
sudo cp ~/blogsystem/deploy/blog.env.example /opt/blogsystem/blog.env

sudo nano /opt/blogsystem/application-prod.yaml   # 改数据库密码、OSS endpoint/bucket
sudo nano /opt/blogsystem/blog.env                # 填 OSS AccessKey

sudo chmod 600 /opt/blogsystem/application-prod.yaml /opt/blogsystem/blog.env
sudo chown -R blogsystem:blogsystem /opt/blogsystem

sudo cp ~/blogsystem/deploy/blog.service /etc/systemd/system/blogsystem.service
sudo systemctl daemon-reload
sudo systemctl enable --now blogsystem
sudo systemctl status blogsystem --no-pager
sudo journalctl -u blogsystem -n 80 --no-pager
```

检查启动日志，确认：

- 没有数据库连接错误；
- 没有 Redis 连接错误；
- 没有 `Could not resolve placeholder 'OSS_ACCESS_KEY_ID'`；
- 进程监听 8080。

快速探活（401 表示应用已启动，只是没带 token）：

```bash
curl -i http://127.0.0.1:8080/user/info
```

## 6. 部署前端

仓库里已经提交了构建好的 `nginx/frontend/dist`，不需要在服务器装 Node：

```bash
sudo mkdir -p /var/www/blogsystem
sudo cp -r ~/blogsystem/nginx/frontend/dist/* /var/www/blogsystem/
sudo chown -R www-data:www-data /var/www/blogsystem   # CentOS 用 nginx:nginx
```

如果需要重新构建前端（修改了源码后）：

```bash
# 安装 Node 20（Ubuntu 自带版本可能过旧）
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs

cd ~/blogsystem/nginx/frontend
npm ci
npm run build

sudo rm -rf /var/www/blogsystem/*
sudo cp -r dist/* /var/www/blogsystem/
```

## 7. 配置 nginx

仓库提供了现成配置：`deploy/nginx-blog.conf`。

```bash
sudo cp ~/blogsystem/deploy/nginx-blog.conf /etc/nginx/sites-available/blogsystem.conf
sudo ln -sf /etc/nginx/sites-available/blogsystem.conf /etc/nginx/sites-enabled/blogsystem.conf
sudo rm -f /etc/nginx/sites-enabled/default
sudo nano /etc/nginx/sites-available/blogsystem.conf   # 改 server_name 为域名或服务器 IP
sudo nginx -t
sudo systemctl reload nginx
```

CentOS/RHEL：

```bash
sudo cp ~/blogsystem/deploy/nginx-blog.conf /etc/nginx/conf.d/blogsystem.conf
sudo nginx -t
sudo systemctl reload nginx
```

如果 CentOS 开了 SELinux，反向代理 502 时执行：

```bash
sudo setsebool -P httpd_can_network_connect 1
```

放行端口：

```bash
sudo ufw allow OpenSSH
sudo ufw allow 'Nginx Full'
sudo ufw enable
```

云服务器还要在**安全组**放行 80/443，不要放行 8080/3306/6379。

## 8. 访问验证

```bash
curl -I http://服务器IP/
curl -i http://服务器IP/user/info     # 应返回 401 JSON
```

浏览器打开 `http://服务器IP/`：

1. 登录页正常显示；
2. 注册/登录；
3. 发布文章、上传图片；
4. 刷新文章详情、个人主页等路由不 404。

### 验证码在哪里看

当前项目没有接真实短信服务，验证码由后端生成后**打印在日志里**：

```bash
sudo journalctl -u blogsystem -f | grep '验证码'
```

日志类似：

```
发送短信验证码成功，验证码：123456
```

登录页的「密码登录」也可以使用注册时设置的密码。

## 9. 配置 HTTPS（推荐）

先把域名 A 记录解析到服务器 IP，然后：

```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d your-domain.com
```

certbot 会自动改写 nginx 配置、申请证书并配置自动续期。完成后用
`https://your-domain.com/` 访问。

## 10. 更新、备份与回滚

### 更新后端

```bash
cd ~/blogsystem
git pull
cd blogSystem
mvn clean package -DskipTests

sudo systemctl stop blogsystem
sudo cp target/blogSystem-0.0.1-SNAPSHOT.jar /opt/blogsystem/
sudo systemctl start blogsystem
sudo systemctl status blogsystem --no-pager
```

### 更新前端

```bash
cd ~/blogsystem
git pull
sudo rm -rf /var/www/blogsystem/*
sudo cp -r nginx/frontend/dist/* /var/www/blogsystem/
sudo systemctl reload nginx
```

### 数据库备份

```bash
mysqldump -u bloguser -p --single-transaction --default-character-set=utf8mb4 blogsystem \
  | gzip > /var/backups/blogsystem-$(date +%F).sql.gz
```

建议加 crontab 每天备份，并把 `/var/backups` 同步到对象存储。

### 回滚

升级前保留旧 jar：

```bash
sudo cp /opt/blogsystem/blogSystem-0.0.1-SNAPSHOT.jar \
        /opt/blogsystem/blogSystem-0.0.1-SNAPSHOT.jar.bak
```

出问题时把 `.bak` 换回来并 `systemctl restart blogsystem`。

## 11. 常见问题

| 现象 | 排查 |
|---|---|
| 502 Bad Gateway | 后端没启动：`systemctl status blogsystem`、`journalctl -u blogsystem`、`ss -lntp \| grep 8080` |
| 所有接口 401 | Redis 没启动或 token 过期：`redis-cli ping`，重新登录 |
| 启动报 `Could not resolve placeholder 'OSS_ACCESS_KEY_ID'` | `/opt/blogsystem/blog.env` 没填 OSS 密钥，或 systemd 没读到该文件 |
| 图片上传失败 / 图片 403 | OSS AccessKey 无权限，或 Bucket 不是公共读 |
| 页面空白 | nginx `root` 不对，或 `/var/www/blogsystem/index.html` 不存在 |
| 刷新 `/article/list` 404 | nginx 缺少 SPA 的 `try_files ... /index.html` |
| 直接访问 `/user/5` 返回 JSON | 使用了旧 nginx 配置，换成 `deploy/nginx-blog.conf` 中的 `/user/{id}` 规则 |
| 上传大图 413 | 调大 nginx `client_max_body_size` 和 Spring `multipart` 限制 |
| 数据库连接失败 | MySQL 未启动、账号 host 不匹配、密码错误 |
| 中文乱码 | 数据库/表使用 `utf8mb4`，导入时加 `--default-character-set=utf8mb4` |

## 12. 已知限制

- **验证码只打印在后端日志**，没有接短信服务；公网多人使用前应接入短信或改为测试码。
- **图片依赖阿里云 OSS**；不配置 OSS 时应用启动会因占位符缺失失败，图片上传也不可用。
- **Redis 是必需组件**，登录、点赞、关注都依赖它。
- 仓库是私有的，服务器 clone 需要 PAT 或部署密钥。
- `application.yaml` 里是开发环境配置；生产请用 `application-prod.yaml` 覆盖，不要把真实密码/密钥提交进 Git。
