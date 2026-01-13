package com.mianzi.showticketsystem.service;

import java.util.Map;

/**
 * 支付服务接口
 */
public interface PaymentService {

    /**
     * 创建支付订单，获取支付二维码
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 包含支付二维码URL的Map，如果失败返回null
     */
    Map<String, String> createPayment(Long orderId, Long userId);

    /**
     * 处理支付宝支付回调
     * @param params 支付宝回调参数
     * @return 处理结果
     */
    String handleAlipayCallback(Map<String, String> params);

    /**
     * 掉单补偿：查询待支付订单并检查支付宝状态
     */
    void compensatePendingOrders();

    /**
     * 超时关单：关闭超过30分钟未支付的订单
     */
    void closeTimeoutOrders();
}

