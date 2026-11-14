-- ============================================================
-- AIOT Nexus BlogSystem - 演示分类数据
-- 不包含任何真实用户、文章或评论。
-- ============================================================

SET NAMES utf8mb4;
USE `blogsystem`;

INSERT INTO `tb_category` (`id`, `name`, `description`, `icon`, `sort_order`) VALUES
    (1, 'AI 人工智能', '大模型、机器学习、深度学习', '🤖', 1),
    (2, 'IoT 物联网', '嵌入式、传感器、边缘计算', '📡', 2),
    (3, '前端开发', 'Vue/React/TypeScript', '🎨', 3),
    (4, '后端开发', 'Spring Boot/MyBatis/Redis', '⚙️', 4),
    (5, '网络安全', '渗透测试、CTF、安全加固', '🔐', 5),
    (6, '科技资讯', '行业动态、产品发布', '📰', 6)
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `description` = VALUES(`description`),
    `icon` = VALUES(`icon`),
    `sort_order` = VALUES(`sort_order`);
