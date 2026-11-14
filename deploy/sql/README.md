# 数据库脚本

| 文件 | 用途 |
|---|---|
| `schema.sql` | 新建数据库的完整表结构（已包含 `email` 字段，不含数据） |
| `seed.sql` | 演示分类数据，不含用户/文章/评论 |
| `2026-11-13-email-login.sql` | 旧库升级脚本，仅给没有 `email` 字段的已有数据库使用 |

## 新数据库

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS blogsystem CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p < deploy/sql/schema.sql
mysql -u root -p < deploy/sql/seed.sql
```

Docker 部署时，把两个文件复制到 MySQL 初始化目录，首次启动会自动执行：

```bash
cp deploy/sql/schema.sql deploy/docker/init/01-schema.sql
cp deploy/sql/seed.sql deploy/docker/init/02-seed.sql
```

> `deploy/docker/init/*.sql` 已被 `.gitignore` 忽略，避免把真实 dump 提交到公开仓库。

## 已有数据库

不要重新执行 `schema.sql`。如果 `tb_user` 还没有 `email` 字段：

```bash
mysql -u bloguser -p blogsystem < deploy/sql/2026-11-13-email-login.sql
```

## 数据说明

- `seed.sql` 只包含 6 个通用分类；
- 真实用户、文章、评论、关注关系不要提交到仓库；
- 公开演示环境建议使用测试账号，不要导入生产 dump。
