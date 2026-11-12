# 把从本机导出的数据库 dump 放到这个目录

将 `blogsystem.sql` 复制到当前目录：

```bash
cp /path/to/blogsystem.sql deploy/docker/init/blogsystem.sql
```

MySQL 容器**首次启动**时会自动执行 `init/*.sql`，把数据导入 `blogsystem` 库。

注意：

- 只有 `mysql_data` 数据卷为空时才会执行；已有数据后不会再执行。
- 如果已经启动过并需要重新导入，执行 `docker compose -f deploy/docker/docker-compose.yml down -v`
  会删除数据库/Redis 数据卷，请先备份。
- 不要把含真实用户数据的 `.sql` 提交到 Git（`.gitignore` 已忽略 `init/*.sql`）。
