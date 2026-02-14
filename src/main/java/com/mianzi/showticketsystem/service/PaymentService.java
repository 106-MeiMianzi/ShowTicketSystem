package com.mianzi.showticketsystem.service;

import java.util.Map;

/**
 * 支付服务接口
 * 
 * 作用：
 * - 定义支付相关的业务逻辑方法
 * - 提供支付订单创建、支付回调处理等功能
 * - 由PaymentServiceImpl实现具体的业务逻辑
 * 
 * 职责：
 * - 支付订单创建
 * - 支付回调处理
 * - 掉单补偿（定时任务）
 * - 超时关单（定时任务）
 */
public interface PaymentService {

    /**
     * 创建支付订单，获取支付二维码
     * 
     * 功能说明：
     * - 用户创建支付订单
     * - 返回支付信息（如支付二维码URL）
     * 
     * @param orderId 订单ID
     * @param userId 用户ID（用于权限校验）
     * @return 包含支付二维码URL的Map，如果失败返回null
     * 
     * 返回内容：
     * - payUrl: 支付URL（实际项目中是支付宝支付页面URL）
     * - orderNo: 商户订单号
     * - message: 提示信息
     */
    Map<String, String> createPayment(Long orderId, Long userId);

    /**
     * 处理支付宝支付回调
     * 
     * 功能说明：
     * - 接收支付宝的支付回调通知
     * - 更新订单状态和支付信息
     * - 发送支付成功邮件通知
     * 
     * @param params 支付宝回调参数（包含订单号、交易号、支付状态等）
     * @return 处理结果（"success"或"failure"）
     * 
     * 返回说明：
     * - 返回"success"表示处理成功，支付宝不会再回调
     * - 返回其他值表示处理失败，支付宝会继续回调
     */
    String handleAlipayCallback(Map<String, String> params);

    /**
     * 掉单补偿：查询待支付订单并检查支付宝状态
     * 
     * 功能说明：
     * - 定时任务，定期检查待支付订单
     * - 查询支付宝订单状态，补偿掉单情况
     * - 如果订单在支付宝中已支付，但系统未更新，则更新订单状态
     * 
     * 说明：
     * - 这是一个定时任务方法，使用@Scheduled注解
     * - 实际项目中需要调用支付宝API查询订单状态
     */
    void compensatePendingOrders();

    /**
     * 超时关单：关闭超过30分钟未支付的订单
     * 
     * 功能说明：
     * - 定时任务，定期检查超时的待支付订单
     * - 关闭超过30分钟未支付的订单
     * - 恢复演出库存
     * 
     * 说明：
     * - 这是一个定时任务方法，使用@Scheduled注解
     * - 实际项目中应该先查询支付宝订单状态，确认未支付后再关闭
     */
    void closeTimeoutOrders();
}
