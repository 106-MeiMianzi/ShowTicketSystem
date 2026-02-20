package com.mianzi.showticketsystem.controller;

import com.mianzi.showticketsystem.model.dto.ApiResponse;
import com.mianzi.showticketsystem.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 支付接口控制器
 * 
 * 作用：
 * - 提供支付相关的API接口
 * - 包括创建支付订单、处理支付回调等功能
 * 
 * 说明：
 * - 创建支付订单需要登录（userId从JWT Token中获取）
 * - 支付回调接口不需要登录（由支付宝服务器调用）
 */
@RestController
/**
 * @RestController 注解：
 * - 标识这是一个REST控制器
 * - 方法返回值自动转换为JSON
 */
@RequestMapping("/api/payment")
/**
 * @RequestMapping 注解：
 * - 定义控制器的基础路径为/api/payment
 * - 所有方法的URL都会以/api/payment开头
 */
public class PaymentController {

    /**
     * 注入支付服务
     * 
     * 用于处理支付相关的业务逻辑
     */
    @Autowired
    private PaymentService paymentService;

    /**
     * 创建支付订单，获取支付二维码
     * 
     * 请求路径: POST /api/payment/create
     * 
     * 功能：
     * - 用户创建支付订单
     * - 返回支付信息（如支付二维码URL）
     * - 需要登录
     * 
     * URL 参数：
     * - orderId: 订单ID（必填）
     * 
     * userId从JWT Token中获取（由JwtAuthenticationFilter设置）
     * 
     * @param orderId 订单ID
     * @param request HTTP请求对象，包含userId属性
     * @return ApiResponse对象，包含支付信息
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createPayment(@RequestParam Long orderId, HttpServletRequest request) {
        /**
         * 从Request属性中获取userId（由JwtAuthenticationFilter设置）
         */
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.failureUnauthorized("请先登录");
        }
        
        /**
         * 调用Service层创建支付订单
         * 
         * 返回支付信息（如支付二维码URL、订单号等）
         */
        Map<String, String> paymentInfo = paymentService.createPayment(orderId, userId);
        if (paymentInfo == null) {
            /**
             * 创建支付订单失败
             * 可能原因：订单不存在、不属于该用户、或状态不可支付
             */
            return ApiResponse.failureBadRequest("创建支付订单失败！订单不存在、不属于您、或状态不可支付。");
        }
        
        /**
         * 创建支付订单成功，返回支付信息
         */
        return ResponseEntity.ok(ApiResponse.success("支付订单创建成功", paymentInfo));
    }

    /**
     * 支付宝支付回调接口
     * 
     * 请求路径: POST /api/payment/notify
     * 
     * 功能：
     * - 接收支付宝的支付回调通知
     * - 更新订单状态和支付信息
     * - 不需要登录（由支付宝服务器调用）
     * 
     * 说明：
     * - 这个接口在JwtAuthenticationFilter的排除列表中
     * - 不需要JWT Token验证
     * - 支付宝会通过POST请求调用此接口，传递支付结果
     * 
     * @param request HTTP请求对象，包含支付宝回调的参数
     * @return 处理结果（success或failure）
     * 
     * 返回说明：
     * - 返回"success"表示处理成功，支付宝不会再回调
     * - 返回其他值表示处理失败，支付宝会继续回调
     */
    @PostMapping("/notify")
    public String handleAlipayNotify(HttpServletRequest request) {
        /**
         * 步骤1：将请求参数转换为Map
         * 
         * 支付宝回调时会传递多个参数（如订单号、交易号、支付状态等）
         * 需要将这些参数提取出来
         */
        Map<String, String> params = new java.util.HashMap<>();
        Map<String, String[]> parameterMap = request.getParameterMap();
        
        /**
         * 遍历请求参数，转换为Map
         * 
         * getParameterMap()返回Map<String, String[]>
         * 需要将String[]转换为String（取第一个值）
         */
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String[] values = entry.getValue();
            if (values != null && values.length > 0) {
                /**
                 * 取第一个值（通常只有一个值）
                 */
                params.put(entry.getKey(), values[0]);
            }
        }

        /**
         * 步骤2：调用Service层处理支付回调
         * 
         * Service层会：
         * 1. 验证回调参数（签名验证等）
         * 2. 根据订单号查找订单
         * 3. 更新订单状态为已支付
         * 4. 保存支付宝交易号
         * 5. 恢复库存（如果需要）
         * 
         * 返回"success"表示处理成功
         */
        return paymentService.handleAlipayCallback(params);
    }
}
