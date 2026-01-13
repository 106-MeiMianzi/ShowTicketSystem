package com.mianzi.showticketsystem.controller;

import com.mianzi.showticketsystem.model.dto.ApiResponse;
import com.mianzi.showticketsystem.model.entity.Order;
import com.mianzi.showticketsystem.model.entity.PageResult;
import com.mianzi.showticketsystem.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
//定义了该控制器类中所有接口的统一根路径
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    //OrderController 就能直接调用 OrderService 中实现的业务逻辑方法
    //而不需要手动去 new OrderServiceImpl()
    private OrderService orderService;

    // --------------------------------------------------------------------------
    // --- 用户端接口 (依赖 URL 参数: userId) ---
    // --------------------------------------------------------------------------

    /**
     * 1. 预订票务 (抢票)
     * URL 参数：showId, quantity
     * userId从JWT Token中获取
     * @return 如果订单创建成功，返回 {"order": {订单详情JSON对象}}；如果失败，返回 {"order": null}
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestParam Long showId,
                                                            @RequestParam Integer quantity,
                                                            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Order order = null;
        if (userId != null) {
            order = orderService.createOrder(userId, showId, quantity);
        }
        // 使用 Map 包装，确保即使 order 为 null 也返回有效的 JSON 对象
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        return ResponseEntity.ok(result);
    }

    /**
     * 2. 查询订单详情
     * URL 参数：orderId
     * userId从JWT Token中获取
     * @return 如果订单存在，返回 {"order": {订单详情JSON对象}}；如果不存在，返回 {"order": null}
     */
    @GetMapping("/details")
    public ResponseEntity<Map<String, Object>> getOrderDetails(@RequestParam Long orderId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Order order = null;
        if (userId != null) {
            order = orderService.getOrderDetails(orderId, userId);
        }
        // 使用 Map 包装，确保即使 order 为 null 也返回有效的 JSON 对象
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        return ResponseEntity.ok(result);
    }

    /**
     * 3. 取消订单
     * URL 参数：orderId
     * userId从JWT Token中获取
     */
    @PutMapping("/cancel")
    public ApiResponse cancelOrder(@RequestParam Long orderId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.failure("取消失败：请先登录。");
        }

        boolean success = orderService.cancelOrder(orderId, userId);
        if (success) {
            return ApiResponse.success("订单取消成功。库存已返还。");
        } else {
            return ApiResponse.failure("取消失败！订单不存在、不属于您、或状态不可取消。");
        }
    }

    /**
     * 4. 支付订单
     * URL 参数：orderId
     * userId从JWT Token中获取
     */
    @PutMapping("/pay")
    public ApiResponse payOrder(@RequestParam Long orderId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.failure("支付失败：请先登录。");
        }

        boolean success = orderService.payOrder(orderId, userId);
        if (success) {
            return ApiResponse.success("订单支付成功！");
        } else {
            return ApiResponse.failure("支付失败！订单不存在、不属于您、或状态不可支付。");
        }
    }

    /**
     * 5. 分页查询指定用户的订单列表
     * URL 参数：pageNum, pageSize
     * userId从JWT Token中获取
     */
    @GetMapping("/list")
    public PageResult<Order> getUserOrderList(@RequestParam(defaultValue = "1") int pageNum,
                                               @RequestParam(defaultValue = "10") int pageSize,
                                               HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            // 返回空的分页结果，而不是null
            return PageResult.build(0, pageNum, pageSize, List.of());
        }
        return orderService.getUserOrderList(userId, pageNum, pageSize);
    }

    /**
     * 用户端 - 条件查询订单列表（分页）
     * URL 参数：status, pageNum, pageSize
     * userId从JWT Token中获取
     */
    @GetMapping("/query")
    public PageResult<Order> getUserOrderListWithConditions(@RequestParam(required = false) Integer status,
                                                             @RequestParam(defaultValue = "1") int pageNum,
                                                             @RequestParam(defaultValue = "10") int pageSize,
                                                             HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            // 返回空的分页结果，而不是null
            return PageResult.build(0, pageNum, pageSize, List.of());
        }
        return orderService.getUserOrderListWithConditions(userId, status, pageNum, pageSize);
    }

    // --------------------------------------------------------------------------
    // --- 管理端接口 (依赖 URL 参数: orderId) ---
    // --------------------------------------------------------------------------

    /**
     * 6. 管理端 - 查询所有订单列表
     */
    @GetMapping("/admin/all")
    public PageResult<Order> getAllOrderList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return orderService.getAllOrderList(pageNum, pageSize);
    }

    /**
     * 管理端 - 条件查询订单列表（分页）
     */
    @GetMapping("/admin/query")
    public PageResult<Order> getAllOrderListWithConditions(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return orderService.getAllOrderListWithConditions(userId, status, pageNum, pageSize);
    }

    /**
     * 7. 管理端 - 手动更新订单状态
     */
    @PutMapping("/admin/status")
    public String updateOrderStatus(
            @RequestParam Long orderId,
            @RequestParam Integer newStatus) {

        //该 SQL 只接收 orderId 和 newStatus，不检查 userId 和 expectedStatus
        boolean success = orderService.updateOrderStatus(orderId, newStatus);

        if (success) {
            return String.format("订单 ID: %d 状态已更新为: %d。", orderId, newStatus);
        } else {
            return String.format("更新失败！订单 ID: %d 不存在或操作失败。", orderId);
        }
    }
}
