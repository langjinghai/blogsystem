# MySQL 初始化脚本目录

MySQL 容器**首次启动**时会按文件名顺序执行本目录下的 `*.sql`，导入到 `blogsystem` 库。

## 方式 A：使用仓库自带 schema + 演示分类（推荐）

```bash
cp deploy/sql/schema.sql deploy/docker/init/01-schema.sql
cp deploy/sql/seed.sql deploy/docker/init/02-seed.sql
```

`schema.sql` 已包含 `email` 字段；`seed.sql` 只有 6 个通用分类，不含真实用户数据。

## 方式 B：使用自己的 dump

```bash
cp /path/to/blogsystem.sql deploy/docker/init/blogsystem.sql
```

如果 dump 是旧版本（`tb_user` 没有 `email` 字段），启动后执行一次：

```bash
cd /opt/blogsystem/deploy/docker
docker compose --env-file .env -f docker-compose.yml exec -T mysql \
  sh -c 'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD" blogsystem' \
  < ../sql/2026-11-13-email-login.sql
```

## 注意

- 只有 `mysql_data` 数据卷为空时才会执行；已有数据后不会重复导入。
- 需要重新导入时先备份，再 `docker compose ... down -v`（会删除数据库和 Redis 数据）。
- 本目录的 `*.sql` 已被 `.gitignore` 忽略，避免把真实 dump 提交到公开仓库。
