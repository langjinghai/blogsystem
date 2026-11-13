-- ============================================================
-- 邮箱登录功能迁移
-- 执行一次即可；请在已有数据卷的 MySQL 中执行
-- 数据库：blogsystem
-- ============================================================

-- 邮箱用户没有手机号，phone 必须允许为空
ALTER TABLE tb_user
    MODIFY COLUMN phone VARCHAR(20) NULL;

-- 新增邮箱字段（唯一）
ALTER TABLE tb_user
    ADD COLUMN email VARCHAR(128) NULL AFTER phone;

ALTER TABLE tb_user
    ADD UNIQUE KEY uk_email (email);
