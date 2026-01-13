-- 票务系统完整数据库初始化脚本
-- 执行方式: mysql -u root -p < init_complete.sql

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS show_ticket_system 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE show_ticket_system;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `email` VARCHAR(100),
    `phone` VARCHAR(20),
    `real_name` VARCHAR(50),
    `role` INT DEFAULT 1 COMMENT '1:普通用户, 2:管理员',
    `status` INT DEFAULT 1 COMMENT '1:正常, 0:禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 演出表
CREATE TABLE IF NOT EXISTS `show` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(200) NOT NULL,
    `venue` VARCHAR(200),
    `region` VARCHAR(50),
    `category` VARCHAR(50),
    `start_time` DATETIME,
    `end_time` DATETIME,
    `total_tickets` INT DEFAULT 0,
    `available_tickets` INT DEFAULT 0,
    `price` DECIMAL(10,2),
    `session_info` TEXT,
    `ticket_tier` TEXT,
    `is_on_sale` INT DEFAULT 0 COMMENT '1:已开票, 0:未开票',
    `status` INT DEFAULT 1 COMMENT '1:正常, 0:已取消',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `show_id` BIGINT NOT NULL,
    `out_trade_no` VARCHAR(64) UNIQUE,
    `alipay_trade_no` VARCHAR(64),
    `quantity` INT NOT NULL,
    `total_price` DECIMAL(10,2) NOT NULL,
    `status` INT DEFAULT 1 COMMENT '1:待支付, 2:已支付, 3:已取消, 4:已退款',
    `order_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `pay_time` DATETIME,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`show_id`) REFERENCES `show`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 收货地址表
CREATE TABLE IF NOT EXISTS `address` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `receiver_name` VARCHAR(50) NOT NULL,
    `receiver_phone` VARCHAR(20) NOT NULL,
    `province` VARCHAR(50),
    `city` VARCHAR(50),
    `district` VARCHAR(50),
    `detail_address` VARCHAR(200),
    `is_default` INT DEFAULT 0 COMMENT '1:默认地址, 0:非默认',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建索引（MySQL 8.0+ 不支持 IF NOT EXISTS，使用存储过程处理）
-- 如果索引已存在会报错，但可以忽略
CREATE INDEX idx_user_username ON `user`(`username`);
CREATE INDEX idx_user_email ON `user`(`email`);
CREATE INDEX idx_show_region ON `show`(`region`);
CREATE INDEX idx_show_category ON `show`(`category`);
CREATE INDEX idx_order_user_id ON `order`(`user_id`);
CREATE INDEX idx_order_show_id ON `order`(`show_id`);
CREATE INDEX idx_order_out_trade_no ON `order`(`out_trade_no`);
CREATE INDEX idx_address_user_id ON `address`(`user_id`);

-- 插入测试管理员账号（可选，用于测试管理端功能）
-- 用户名: admin, 密码: admin123
INSERT INTO `user` (`username`, `password`, `email`, `role`, `status`) 
VALUES ('admin', 'admin123', 'admin@example.com', 2, 1)
ON DUPLICATE KEY UPDATE `username`=`username`;




