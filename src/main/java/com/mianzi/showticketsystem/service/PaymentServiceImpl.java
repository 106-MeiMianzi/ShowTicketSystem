package com.mianzi.showticketsystem.service;

import com.mianzi.showticketsystem.mapper.OrderMapper;
import com.mianzi.showticketsystem.mapper.ShowMapper;
import com.mianzi.showticketsystem.mapper.UserMapper;
import com.mianzi.showticketsystem.model.entity.Order;
import com.mianzi.showticketsystem.model.entity.Show;
import com.mianzi.showticketsystem.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付服务实现类（支付宝沙箱环境）
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ShowMapper showMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 实现创建支付订单的逻辑（简化版本，不依赖支付宝SDK）
     * 注意：这是一个模拟实现，实际项目中需要集成真实的支付SDK
     */
    @Override
    public Map<String, String> createPayment(Long orderId, Long userId) {
        // 查询订单
        Order order = orderMapper.getByIdAndUserId(orderId, userId);
        if (order == null || order.getStatus() != 1) {
            return null; // 订单不存在或状态不对
        }
        // 双重验证：确保返回的订单确实属于当前用户（防止SQL注入或其他安全问题）
        if (!order.getUserId().equals(userId)) {
            return null; // 订单不属于当前用户
        }

        // 简化版本：返回模拟的支付URL
        // 实际项目中，这里应该调用支付宝SDK创建支付订单
        Map<String, String> result = new HashMap<>();
        result.put("payUrl", "http://localhost:8080/payment/mock?orderNo=" + order.getOutTradeNo());
        result.put("orderNo", order.getOutTradeNo());
        result.put("message", "这是模拟支付URL，实际项目中需要集成支付宝SDK");
        return result;
    }

    /**
     * 实现处理支付宝支付回调的逻辑
     */
    @Override
    @Transactional
    public String handleAlipayCallback(Map<String, String> params) {
        try {
            // 验证签名（实际项目中需要验证）
            // 这里简化处理，实际应该使用支付宝SDK验证签名

            String outTradeNo = params.get("out_trade_no");
            String tradeStatus = params.get("trade_status");
            String tradeNo = params.get("trade_no"); // 支付宝交易号

            if (outTradeNo == null) {
                return "failure";
            }

            Order order = orderMapper.getByOutTradeNo(outTradeNo);
            if (order == null) {
                return "failure";
            }

            // 如果订单已支付，直接返回成功
            if (order.getStatus() == 2) {
                return "success";
            }

            // 处理支付成功
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                // 更新订单状态
                orderMapper.updateStatusAndPayTime(
                        order.getId(),
                        order.getUserId(),
                        2, // 已支付
                        1  // 待支付
                );

                // 更新支付宝交易号
                orderMapper.updateAlipayTradeNo(order.getId(), tradeNo);

                // 发送邮件通知（需要查询用户和演出信息）
                User user = userMapper.selectById(order.getUserId());
                if (user != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
                    // 查询演出信息
                    Show show = showMapper.getById(order.getShowId());
                    String showName = show != null ? show.getName() : "演出票务";
                    emailService.sendPaymentSuccessEmail(
                            user.getEmail(),
                            order.getOutTradeNo(),
                            showName,
                            order.getTotalPrice()
                    );
                }

                return "success";
            }

            return "failure";
        } catch (Exception e) {
            System.err.println("处理支付回调失败: " + e.getMessage());
            return "failure";
        }
    }

    /**
     * 掉单补偿：查询待支付订单并检查支付宝状态（简化版本）
     * 注意：实际项目中需要调用支付宝API查询订单状态
     */
    @Override
    @Scheduled(fixedRate = 300000) // 每5分钟执行一次
    public void compensatePendingOrders() {
        // 查询30分钟前的待支付订单
        LocalDateTime beforeTime = LocalDateTime.now().minusMinutes(30);
        List<Order> pendingOrders = orderMapper.findPendingOrdersBefore(beforeTime);

        // 简化版本：这里应该调用支付宝API查询订单状态
        // 实际项目中，需要集成支付宝SDK来查询订单状态
        System.out.println("掉单补偿任务执行，待处理订单数: " + pendingOrders.size());
        // TODO: 集成支付宝SDK后，在这里调用支付宝API查询订单状态
    }

    /**
     * 超时关单：关闭超过30分钟未支付的订单（简化版本）
     * 注意：实际项目中需要先查询支付宝订单状态，确认未支付后再关闭
     */
    @Override
    @Scheduled(fixedRate = 600000) // 每10分钟执行一次
    @Transactional
    public void closeTimeoutOrders() {
        // 查询30分钟前的待支付订单
        LocalDateTime beforeTime = LocalDateTime.now().minusMinutes(30);
        List<Order> pendingOrders = orderMapper.findPendingOrdersBefore(beforeTime);

        // 简化版本：直接关闭超时订单
        // 实际项目中，应该先调用支付宝API查询订单状态，确认未支付后再关闭
        for (Order order : pendingOrders) {
            try {
                // TODO: 实际项目中，这里应该先调用支付宝API查询订单状态
                // 如果订单在支付宝中未支付，才执行以下关闭操作
                
                // 更新订单状态为已取消（3=已取消，1=待支付）
                orderMapper.updateStatus(order.getId(), order.getUserId(), 3, 1);
                // 返还库存
                showMapper.addStock(order.getShowId(), order.getQuantity());
            } catch (Exception e) {
                System.err.println("关闭订单失败: " + e.getMessage());
            }
        }
    }
}

