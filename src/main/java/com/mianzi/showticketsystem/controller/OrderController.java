package com.mianzi.showticketsystem.controller;

import com.mianzi.showticketsystem.model.entity.Order;
import com.mianzi.showticketsystem.model.entity.PageResult;
import com.mianzi.showticketsystem.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // --------------------------------------------------------------------------
    // --- 用户端接口 (依赖 URL 参数: userId) ---
    // --------------------------------------------------------------------------

    /**
     * 1. 预订票务 (抢票)
     * URL 参数：showId, quantity, userId
     */
    @PostMapping("/create")
    public Order createOrder(
            @RequestParam Long showId,
            @RequestParam Integer quantity,
            @RequestParam Long userId) {

        return orderService.createOrder(userId, showId, quantity);
    }

    /**
     * 2. 查询订单详情
     * URL 参数：orderId, userId
     */
    @GetMapping("/details")
    public Order getOrderDetails(
            @RequestParam Long orderId,
            @RequestParam Long userId) {

        // Service 层需要 userId 进行权限校验（只能查自己的订单）
        return orderService.getOrderDetails(orderId, userId);
    }

    /**
     * 3. 取消订单
     * URL 参数：orderId, userId
     */
    @PutMapping("/cancel")
    public String cancelOrder(
            @RequestParam Long orderId,
            @RequestParam Long userId) {

        boolean success = orderService.cancelOrder(orderId, userId);

        if (success) {
            return "订单取消成功。库存已返还。";
        } else {
            return "取消失败！订单不存在、不属于您、或状态不可取消。";
        }
    }

    /**
     * 4. 支付订单
     * URL 参数：orderId, userId
     */
    @PutMapping("/pay")
    public String payOrder(
            @RequestParam Long orderId,
            @RequestParam Long userId) {

        boolean success = orderService.payOrder(orderId, userId);

        if (success) {
            return "订单支付成功！";
        } else {
            return "支付失败！订单不存在、不属于您、或状态不可支付。";
        }
    }

    /**
     * 5. 分页查询指定用户的订单列表
     * URL 参数：userId, pageNum, pageSize
     */
    @GetMapping("/list")
    public PageResult<Order> getUserOrderList(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        return orderService.getUserOrderList(userId, pageNum, pageSize);
    }

    // --------------------------------------------------------------------------
    // --- 管理端接口 (依赖 URL 参数: orderId) ---
    // --------------------------------------------------------------------------

    /**
     * 6. 管理端 - 查询所有订单列表
     * 不依赖 userId，需要管理员账户才能访问 (假设通过其他机制或不验证)
     */
    @GetMapping("/admin/all")
    public PageResult<Order> getAllOrderList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        return orderService.getAllOrderList(pageNum, pageSize);
    }

    /**
     * 7. 管理端 - 手动更新订单状态
     */
    @PutMapping("/admin/status")
    public String updateOrderStatus(
            @RequestParam Long orderId,
            @RequestParam Integer newStatus) {

        boolean success = orderService.updateOrderStatus(orderId, newStatus);

        if (success) {
            return String.format("订单 ID: %d 状态已更新为: %d。", orderId, newStatus);
        } else {
            return String.format("更新失败！订单 ID: %d 不存在或操作失败。", orderId);
        }
    }
}
