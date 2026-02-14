package com.mianzi.showticketsystem.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 * 
 * 对应数据库表：order
 * 
 * 作用：
 * - 映射数据库中的订单表结构
 * - 用于在 Java 代码中表示订单数据
 * - 记录用户的购票订单信息
 */
@Data
/**
 * @Data 注解：自动生成 getter、setter、toString、equals、hashCode 等方法
 */
@Accessors(chain = true)
/**
 * @Accessors(chain = true) 注解：启用链式调用
 */
public class Order {

    /**
     * 订单ID（主键，自增）
     * 
     * 数据库字段：id BIGINT AUTO_INCREMENT PRIMARY KEY
     * 
     * 作用：唯一标识一个订单
     */
    private Long id;

    /**
     * 用户ID（外键）
     * 
     * 数据库字段：user_id BIGINT NOT NULL
     * 
     * 外键关系：
     * - 关联 user 表的 id 字段
     * - FOREIGN KEY (user_id) REFERENCES user(id)
     * 
     * 作用：标识这个订单属于哪个用户
     */
    private Long userId;

    /**
     * 演出ID（外键）
     * 
     * 数据库字段：show_id BIGINT NOT NULL
     * 
     * 外键关系：
     * - 关联 show 表的 id 字段
     * - FOREIGN KEY (show_id) REFERENCES show(id)
     * 
     * 作用：标识这个订单购买的是哪个演出
     */
    private Long showId;

    /**
     * 商户订单号（用于支付，唯一标识）
     * 
     * 数据库字段：out_trade_no VARCHAR(64) UNIQUE
     * 
     * 说明：
     * - 系统生成的唯一订单号，用于支付接口
     * - 格式通常为：时间戳 + 随机数，如 "20240101123456789012345678"
     * - UNIQUE 约束确保唯一性
     * 
     * 用途：
     * - 调用支付接口时作为商户订单号
     * - 支付回调时用于查询订单
     */
    private String outTradeNo;

    /**
     * 支付宝交易号
     * 
     * 数据库字段：alipay_trade_no VARCHAR(64)
     * 
     * 说明：
     * - 支付宝返回的交易号
     * - 支付成功后由支付宝回调接口填充
     * - 用于查询支付详情和退款
     */
    private String alipayTradeNo;

    /**
     * 购买数量
     * 
     * 数据库字段：quantity INT NOT NULL
     * 
     * 说明：用户购买的票数
     * 
     * 业务逻辑：
     * - 下单时检查 availableTickets >= quantity
     * - 支付成功后减少演出的 availableTickets
     */
    private Integer quantity;

    /**
     * 订单总金额
     * 
     * 数据库字段：total_price DECIMAL(10,2) NOT NULL
     * 
     * 类型说明：
     * - 使用 BigDecimal 而不是 double/float
     * - 原因：金额计算需要精确，避免浮点数精度问题
     * 
     * 计算方式：
     * - totalPrice = show.price * quantity
     * - 下单时计算并保存
     */
    private BigDecimal totalPrice;

    /**
     * 订单状态
     * 
     * 数据库字段：status INT DEFAULT 1
     * 
     * 状态说明：
     * - 1：待支付 - 订单已创建，等待用户支付
     * - 2：已支付 - 用户已成功支付
     * - 3：已取消 - 订单被取消（用户取消或超时）
     * - 4：已退款 - 订单已退款
     * 
     * 状态流转：
     * 待支付 -> 已支付（支付成功）
     * 待支付 -> 已取消（用户取消或超时）
     * 已支付 -> 已退款（退款操作）
     */
    private Integer status;

    /**
     * 下单时间
     * 
     * 数据库字段：order_time DATETIME DEFAULT CURRENT_TIMESTAMP
     * 
     * 说明：用户创建订单的时间
     */
    private LocalDateTime orderTime;

    /**
     * 支付时间
     * 
     * 数据库字段：pay_time DATETIME
     * 
     * 说明：
     * - 用户完成支付的时间
     * - 支付成功后由支付回调接口填充
     * - 如果未支付，此字段为 null
     */
    private LocalDateTime payTime;

    /**
     * 创建时间
     * 
     * 数据库字段：create_time DATETIME DEFAULT CURRENT_TIMESTAMP
     * 
     * 说明：记录订单创建的时间（通常与 orderTime 相同）
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     * 
     * 数据库字段：update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
     * 
     * 说明：记录订单信息最后一次修改的时间
     */
    private LocalDateTime updateTime;
}
