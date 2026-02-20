-- 为 user 表添加微信 openid 字段
-- 执行此 SQL 脚本以支持微信扫码登录功能

USE show_ticket_system;

-- 添加微信 openid 字段
ALTER TABLE `user` ADD COLUMN `wechat_openid` VARCHAR(64) UNIQUE COMMENT '微信openid，用于扫码登录';

-- 创建索引以提高查询性能
CREATE INDEX `idx_user_wechat_openid` ON `user`(`wechat_openid`);
