package com.mianzi.showticketsystem.service;

/**
 * 邮件服务接口
 */
public interface EmailService {

    /**
     * 发送支付成功通知邮件
     * @param to 收件人邮箱
     * @param orderNo 订单号
     * @param showName 演出名称
     * @param totalPrice 订单总金额
     */
    void sendPaymentSuccessEmail(String to, String orderNo, String showName, java.math.BigDecimal totalPrice);
}

