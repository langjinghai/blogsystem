# AIOT Nexus — BlogSystem

一个前后端分离的博客系统：Spring Boot 提供 REST API，Vue 3 提供管理界面，MySQL 持久化，Redis 保存登录态与点赞/关注缓存，图片上传到阿里云 OSS。

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Spring Boot 2.7.18、MyBatis-Plus、MySQL 8、Redis、阿里云 OSS |
| 前端 | Vue 3、TypeScript、Vite、Element Plus、Pinia、Vue Router |
| 部署 | nginx（静态资源 + 反向代理）、systemd、Docker Compose |

## 目录结构

```
blogSystem/              Spring Boot 后端
  src/main/java/         Controller / Service / Mapper / 配置
  src/main/resources/    application.yaml、Mapper XML
nginx/
  conf/nginx.conf        本机 Windows 运行配置
  frontend/              Vue 3 前端源码
  frontend/dist/         已构建的前端产物（nginx 直接托管）
deploy/
  DEPLOY.md              服务器部署指南
  nginx-blog.conf        Linux nginx 站点配置模板
  blog.service           systemd 服务模板
  application-prod.yaml.example  生产配置模板
  blog.env.example       OSS/SMTP 等环境变量模板
  sql/                   数据库增量迁移脚本
  docker/                Docker Compose 部署
    docker-compose.yml
    Dockerfile.backend
    Dockerfile.frontend
    nginx.conf
    README.md
```

## 本地运行（简要）

1. 复制本地配置并填写数据库密码/Redis 地址（该文件已被 `.gitignore` 忽略）：
   ```bash
   cp blogSystem/application-local.yaml.example blogSystem/application-local.yaml
   ```
2. 启动 MySQL，创建 `blogsystem` 库并导入数据。
3. 启动 Redis（默认 `127.0.0.1:6379`）。
4. 后端：用 JDK 11/17 运行 `blogSystem`，默认端口 8080。
5. 前端：`cd nginx/frontend && npm install && npm run dev`，或在 IntelliJ 中启动。
6. 浏览器访问 Vite 地址（默认 5173），API 由 Vite 代理到 8080。

> 图片上传需要配置阿里云 OSS 的 `OSS_ACCESS_KEY_ID` / `OSS_ACCESS_KEY_SECRET`。
> 手机号验证码会打印在后端日志中；邮箱登录会通过 QQ 邮箱 SMTP 真实发信。
> 邮箱登录需配置 `MAIL_USERNAME` / `MAIL_PASSWORD`（QQ 邮箱授权码），不配则只能使用手机号登录。
> 已有数据库需要先执行 `deploy/sql/2026-11-13-email-login.sql` 增加 `tb_user.email` 字段。

## 安全说明

- 密码使用 **BCrypt** 哈希存储；首次启动时 `PasswordMigrationRunner` 会把存量明文密码自动迁移为 BCrypt，用户密码本身不变。
- 文章正文经 `marked` 渲染后由 **DOMPurify** 消毒；nginx 同时下发 CSP、X-Frame-Options、nosniff 等安全响应头。
- 修改/重置密码后，该用户所有旧 token 立即失效（Redis token 版本机制）。
- 数据库密码、Redis 地址等写在 `blogSystem/application-local.yaml`（已忽略），不要提交；生产使用 `application-prod.yaml` 或环境变量。
- **验证码日志仅用于没有短信服务的演示环境**。如果部署到公网，必须接入真实短信并移除验证码日志，否则能看日志的人可以登录任意账号。

## 部署到服务器

### 方式一：Docker Compose（推荐）

一条命令拉起 MySQL、Redis、后端和 nginx：

```bash
cp deploy/docker/.env.example deploy/docker/.env   # 填写密码和 OSS 密钥
cp /path/to/blogsystem.sql deploy/docker/init/blogsystem.sql
docker compose --env-file deploy/docker/.env -f deploy/docker/docker-compose.yml up -d --build
```

详细说明见 **[deploy/docker/README.md](deploy/docker/README.md)**。

### 方式二：直接部署到宿主机

完整的 Ubuntu/CentOS 部署步骤、数据库导出、systemd、nginx、HTTPS、备份与排错见：

**[deploy/DEPLOY.md](deploy/DEPLOY.md)**

## 说明

- `application.yaml` 里是开发环境配置，生产环境请用 `deploy/application-prod.yaml.example` 覆盖，不要把真实数据库密码和 OSS 密钥提交到仓库。
- 仓库为私有仓库；服务器 clone 需要 GitHub PAT 或部署密钥。
