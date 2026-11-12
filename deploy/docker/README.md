# Docker Compose 部署

一条 `docker compose` 命令拉起 MySQL、Redis、Spring Boot 后端和 nginx 前端，服务器只需要安装 Docker。

## 组成

| 服务 | 镜像/构建 | 说明 |
|---|---|---|
| `mysql` | `mysql:8.0` | 数据卷 `mysql_data`，首次启动自动导入 `init/*.sql` |
| `redis` | `redis:7-alpine` | 数据卷 `redis_data`，AOF 持久化 |
| `backend` | `deploy/docker/Dockerfile.backend` | Maven 多阶段构建，JDK 17 运行 |
| `web` | `deploy/docker/Dockerfile.frontend` | Node 构建前端 + nginx 托管/反代 |

只有 `web` 映射宿主机 80 端口；MySQL、Redis、后端都只在 Compose 内部网络，不暴露公网。

## 前置条件

- Docker 24+ 与 Compose v2（`docker compose version` 可用）。
- 服务器 80 端口空闲（如果已有宿主机 nginx，见下面「端口与 HTTPS」）。
- 能拉取 Docker Hub 镜像；国内服务器建议给 Docker 配置镜像加速。
- 从本机导出的 `blogsystem.sql`。
- 阿里云 OSS 的 AccessKey（图片上传用，必填）。

## 1. 准备 .env

```bash
cd /path/to/blogsystem
cp deploy/docker/.env.example deploy/docker/.env
nano deploy/docker/.env
chmod 600 deploy/docker/.env
```

至少填写：

```dotenv
MYSQL_ROOT_PASSWORD=强密码
MYSQL_USER=bloguser
MYSQL_PASSWORD=强密码
OSS_ACCESS_KEY_ID=你的AccessKeyId
OSS_ACCESS_KEY_SECRET=你的AccessKeySecret
```

> `.env` 已被 `.gitignore` 忽略，不要提交真实密钥。

## 2. 放入数据库 dump

在本机导出（PowerShell）：

```powershell
& 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe' `
  --user=root --password `
  --single-transaction --default-character-set=utf8mb4 --set-gtid-purged=OFF `
  --routines --triggers `
  --result-file="D:\blogsystem.sql" blogsystem
```

上传并放到 init 目录：

```bash
scp D:\blogsystem.sql 用户名@服务器IP:/tmp/blogsystem.sql
cp /tmp/blogsystem.sql deploy/docker/init/blogsystem.sql
```

MySQL 容器**首次启动**时会自动执行 `init/*.sql`，导入到 `blogsystem` 库。

注意：

- 只有 `mysql_data` 数据卷为空时才会执行，之后不会重复导入。
- 需要重新导入时先备份，再 `docker compose ... down -v`（会删除数据库和 Redis 数据）。
- 不要把含真实用户数据的 SQL 提交到 Git（`.gitignore` 已忽略 `init/*.sql`）。

## 3. 启动

```bash
docker compose --env-file deploy/docker/.env -f deploy/docker/docker-compose.yml up -d --build
```

首次会构建后端和前端镜像，并拉取 MySQL/Redis 镜像，通常需要几分钟。

查看状态与日志：

```bash
docker compose --env-file deploy/docker/.env -f deploy/docker/docker-compose.yml ps
docker compose --env-file deploy/docker/.env -f deploy/docker/docker-compose.yml logs -f backend
```

`backend` 的健康检查会等待 8080 端口可用；`web` 会等 backend 健康后再启动。

## 4. 验证

```bash
curl -I http://127.0.0.1/
curl -i http://127.0.0.1/user/info     # 401 表示后端已启动
```

浏览器打开 `http://服务器IP/`，测试：

1. 登录页显示；
2. 注册/登录；
3. 发布文章、上传图片；
4. 刷新文章详情、个人主页不 404。

验证码没有接短信服务，会打印在后端日志：

```bash
docker compose --env-file deploy/docker/.env -f deploy/docker/docker-compose.yml logs -f backend | grep '验证码'
```

## 5. 端口与 HTTPS

默认 `web` 映射 `80:80`。如果服务器上已经有宿主机 nginx 占用了 80：

方案 A（推荐）：让宿主机 nginx 只做 HTTPS 终止，Docker 的 web 只监听本机端口。

1. 把 `docker-compose.yml` 中：

```yaml
ports:
  - "80:80"
```

改成：

```yaml
ports:
  - "127.0.0.1:8081:80"
```

2. 宿主机 nginx 增加一个站点，`proxy_pass http://127.0.0.1:8081;`；
3. 用 `certbot --nginx -d your-domain.com` 申请证书。

方案 B：停掉宿主机 nginx，直接让 Docker 的 web 占用 80：

```bash
sudo systemctl stop nginx
sudo systemctl disable nginx
```

纯 Docker 的 TLS（Caddy/Traefik/云负载均衡）也可以，但不在本仓库默认配置内。

## 6. 数据、备份与恢复

数据卷：

- `blogsystem_mysql_data`
- `blogsystem_redis_data`

备份数据库：

```bash
docker compose --env-file deploy/docker/.env -f deploy/docker/docker-compose.yml \
  exec -T mysql sh -c 'exec mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --single-transaction --default-character-set=utf8mb4 blogsystem' \
  > blogsystem-$(date +%F).sql
```

恢复：

```bash
docker compose --env-file deploy/docker/.env -f deploy/docker/docker-compose.yml \
  exec -T mysql sh -c 'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD" blogsystem' \
  < blogsystem-2026-11-13.sql
```

停止但保留数据：

```bash
docker compose --env-file deploy/docker/.env -f deploy/docker/docker-compose.yml down
```

连数据一起删除（危险）：

```bash
docker compose --env-file deploy/docker/.env -f deploy/docker/docker-compose.yml down -v
```

## 7. 更新

```bash
cd /path/to/blogsystem
git pull
docker compose --env-file deploy/docker/.env -f deploy/docker/docker-compose.yml up -d --build
```

只更新后端：

```bash
docker compose --env-file deploy/docker/.env -f deploy/docker/docker-compose.yml up -d --build backend
```

nginx 配置里使用了 Docker 内置 DNS（`resolver 127.0.0.11`）和变量 `proxy_pass`，后端容器 IP 变化后会自动重新解析；如遇 502，可再 `... restart web`。

## 8. 常见问题

| 现象 | 排查 |
|---|---|
| `backend` 一直 unhealthy | `docker compose ... logs backend`；检查 `.env` 里 OSS 密钥、MySQL 密码、数据库是否导入 |
| 502 Bad Gateway | `docker compose ... ps` 看 backend 是否 healthy；`docker compose ... logs backend` |
| 页面 404/空白 | `docker compose ... logs web`；确认前端镜像构建成功 |
| MySQL 没有导入 SQL | 只会在空数据卷首次启动时执行；`down -v` 后重新 `up`（先备份） |
| 80 端口被占用 | `ss -lntp \| grep :80`；停宿主机 nginx 或改映射端口 |
| 拉取镜像慢/失败 | 给 Docker 配置镜像加速器；或先手动 `docker pull` 基础镜像 |
| Maven 构建慢 | 已内置阿里云 Maven 镜像 `deploy/docker/maven-settings.xml` |
| npm 构建慢 | 在 `.env` 设置 `NPM_REGISTRY=https://registry.npmmirror.com` |
| 上传图片失败 | OSS AccessKey/权限；Bucket 需公共读，否则图片 URL 403 |
| 上传大图 413 | 调大 compose 中 nginx `client_max_body_size` 和 Spring multipart 限制 |
| 接口报 `Could not initialize class com.baomidou.mybatisplus.core.toolkit.support.SerializedLambdaMeta` | JDK17 + MyBatis-Plus 3.4.3 的模块访问问题；compose 已通过 `JAVA_TOOL_OPTIONS` 加 `--add-opens`，重启 backend 即可 |
| 直接访问 `/user/5` 返回 JSON | 使用本目录 `nginx.conf`（已按 Accept 区分 SPA/API） |

## 9. 安全建议

- `.env` 权限 600，且不要提交到 Git。
- MySQL、Redis、backend 不要加 `ports` 暴露到公网。
- OSS 使用最小权限 AccessKey；Bucket 公共读只用于图片。
- 生产环境建议在宿主机 nginx 或云负载均衡上做 HTTPS。
- 私有仓库在服务器 clone 需要 PAT 或部署密钥。
