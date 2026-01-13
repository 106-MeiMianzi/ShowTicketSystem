package com.mianzi.showticketsystem.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import java.math.BigDecimal; // 注意：使用 BigDecimal 处理金额
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Data
@Accessors(chain = true)
public class Order {

    private Long id; // 主键ID
    private Long userId; // 用户ID
    private Long showId; // 演出ID
    private String outTradeNo; // 商户订单号（用于支付，唯一标识）
    private String alipayTradeNo; // 支付宝交易号
    private Integer quantity; // 购买数量
    private BigDecimal totalPrice; // 订单总金额
    private Integer status; // 订单状态 (1: 待支付, 2: 已支付, 3: 已取消, 4: 已退款)
    private LocalDateTime orderTime; // 下单时间
    private LocalDateTime payTime; // 支付时间
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}
