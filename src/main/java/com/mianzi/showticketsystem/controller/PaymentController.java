package com.mianzi.showticketsystem.controller;

import com.mianzi.showticketsystem.model.dto.ApiResponse;
import com.mianzi.showticketsystem.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 支付接口控制器
 */
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * 创建支付订单，获取支付二维码
     * 请求路径: POST /api/payment/create
     * @param orderId 订单ID
     * @param request HTTP请求（userId从JWT Token中获取）
     * @return 包含支付信息的ApiResponse
     */
    @PostMapping("/create")
    public ApiResponse createPayment(@RequestParam Long orderId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.failure("请先登录");
        }
        
        Map<String, String> paymentInfo = paymentService.createPayment(orderId, userId);
        if (paymentInfo == null) {
            return ApiResponse.failure("创建支付订单失败！订单不存在、不属于您、或状态不可支付。");
        }
        
        return ApiResponse.success("支付订单创建成功", paymentInfo);
    }

    /**
     * 支付宝支付回调接口
     * 请求路径: POST /api/payment/notify
     * @param request HTTP请求
     * @return 处理结果（success或failure）
     */
    @PostMapping("/notify")
    public String handleAlipayNotify(HttpServletRequest request) {
        // 将请求参数转换为Map
        Map<String, String> params = new java.util.HashMap<>();
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String[] values = entry.getValue();
            if (values != null && values.length > 0) {
                params.put(entry.getKey(), values[0]);
            }
        }

        return paymentService.handleAlipayCallback(params);
    }
}

