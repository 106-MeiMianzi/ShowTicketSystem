-- 为已有数据库增加封面图字段（仅当表已存在但无 cover_url 时执行一次）
-- 执行前请确认：USE show_ticket_system;
-- 若报错 Duplicate column name 'cover_url' 说明已加过，可忽略。
--
-- 执行方式（任选其一）：
-- 1) 命令行：mysql -u root -p show_ticket_system < src/main/resources/db/migration_add_cover_url.sql
-- 2) Docker MySQL：docker exec -i showticket-mysql mysql -u root -p'@showticket' show_ticket_system < src/main/resources/db/migration_add_cover_url.sql
-- 3) 在 MySQL 客户端或 Navicat 等工具中执行下面这一句：

ALTER TABLE `show` ADD COLUMN `cover_url` VARCHAR(512) NULL COMMENT '封面图URL' AFTER `ticket_tier`;
