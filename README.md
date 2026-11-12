# AIOT Nexus — BlogSystem

一个前后端分离的博客系统：Spring Boot 提供 REST API，Vue 3 提供管理界面，MySQL 持久化，Redis 保存登录态与点赞/关注缓存，图片上传到阿里云 OSS。

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Spring Boot 2.7.18、MyBatis-Plus、MySQL 8、Redis、阿里云 OSS |
| 前端 | Vue 3、TypeScript、Vite、Element Plus、Pinia、Vue Router |
| 部署 | nginx（静态资源 + 反向代理）、systemd |

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
  blog.env.example       OSS 密钥等环境变量模板
```

## 本地运行（简要）

1. 启动 MySQL，创建 `blogsystem` 库并导入数据。
2. 启动 Redis（默认 `127.0.0.1:6379`）。
3. 后端：用 JDK 11/17 运行 `blogSystem`，默认端口 8080。
4. 前端：`cd nginx/frontend && npm install && npm run dev`，或在 IntelliJ 中启动。
5. 浏览器访问 Vite 地址（默认 5173），API 由 Vite 代理到 8080。

> 图片上传需要配置阿里云 OSS 的 `OSS_ACCESS_KEY_ID` / `OSS_ACCESS_KEY_SECRET`。
> 当前验证码会打印在后端日志中，未接入真实短信服务。

## 部署到服务器

完整的 Ubuntu/CentOS 部署步骤、数据库导出、systemd、nginx、HTTPS、备份与排错见：

**[deploy/DEPLOY.md](deploy/DEPLOY.md)**

## 说明

- `application.yaml` 里是开发环境配置，生产环境请用 `deploy/application-prod.yaml.example` 覆盖，不要把真实数据库密码和 OSS 密钥提交到仓库。
- 仓库为私有仓库；服务器 clone 需要 GitHub PAT 或部署密钥。
