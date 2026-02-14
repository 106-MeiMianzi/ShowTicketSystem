package com.mianzi.showticketsystem.service;

/**
 * 邮件服务接口
 * 
 * 作用：
 * - 定义邮件发送相关的业务逻辑方法
 * - 提供邮件发送功能
 * - 由EmailServiceImpl实现具体的业务逻辑
 * 
 * 职责：
 * - 发送支付成功通知邮件
 * - 可以扩展其他邮件通知功能（如订单确认、密码重置等）
 */
public interface EmailService {

    /**
     * 发送支付成功通知邮件
     * 
     * 功能说明：
     * - 用户支付成功后，发送邮件通知
     * - 包含订单信息和演出信息
     * 
     * @param to 收件人邮箱
     * @param orderNo 订单号
     * @param showName 演出名称
     * @param totalPrice 订单总金额
     * 
     * 说明：
     * - 如果邮件服务未配置，方法会静默失败（不影响主流程）
     * - 邮件发送失败不应该影响订单处理流程
     */
    void sendPaymentSuccessEmail(String to, String orderNo, String showName, java.math.BigDecimal totalPrice);
}
