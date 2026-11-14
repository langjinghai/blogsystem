-- ============================================================
-- AIOT Nexus BlogSystem - 数据库结构
-- 适用于 MySQL 8.0+
--
-- 新数据库：
--   mysql -u root -p < deploy/sql/schema.sql
--   mysql -u root -p < deploy/sql/seed.sql
--
-- 已有数据库：不要执行本文件，按需执行 deploy/sql/2026-11-13-email-login.sql
-- 本文件只包含表结构，不包含任何真实用户数据。
-- ============================================================

SET NAMES utf8mb4;

-- 数据库需要提前存在：
-- Docker 会通过 MYSQL_DATABASE 自动创建；
-- 原生部署请先用 root 执行 CREATE DATABASE blogsystem。
USE `blogsystem`;

-- ------------------------------------------------------------
-- 文章
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_article` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文章ID',
    `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
    `summary` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '摘要',
    `content` mediumtext COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '正文（Markdown）',
    `cover_image` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '封面图URL',
    `category_id` bigint NOT NULL COMMENT '分类ID',
    `author_id` bigint NOT NULL COMMENT '作者ID',
    `view_count` int NOT NULL DEFAULT '0' COMMENT '浏览量',
    `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数（冗余）',
    `comment_count` int NOT NULL DEFAULT '0' COMMENT '评论数（冗余）',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_author_id` (`author_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_view_count` (`view_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';

-- ------------------------------------------------------------
-- 分类
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_category` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
    `description` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分类描述',
    `icon` varchar(8) COLLATE utf8mb4_unicode_ci DEFAULT 0xF09F9382 COMMENT '分类图标（emoji）',
    `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序权重（越小越靠前）',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- ------------------------------------------------------------
-- 评论（parent_id 支持回复）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_comment` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `article_id` bigint NOT NULL COMMENT '文章ID',
    `content` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
    `author_id` bigint NOT NULL COMMENT '评论人ID',
    `parent_id` bigint DEFAULT NULL COMMENT '父评论ID（支持回复）',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_article_id` (`article_id`),
    KEY `idx_author_id` (`author_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- ------------------------------------------------------------
-- 关注关系
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_follow` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `follower_id` bigint NOT NULL COMMENT '关注者ID',
    `followee_id` bigint NOT NULL COMMENT '被关注者ID',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_follower_followee` (`follower_id`, `followee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关注关系表';

-- ------------------------------------------------------------
-- 用户（手机号/邮箱共用一张表，二者都可为空）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_user` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
    `email` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
    `password` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（BCrypt）',
    `nick_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
    `icon` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像URL',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
