package com.mianzi.showticketsystem.controller;

import com.mianzi.showticketsystem.annotation.RateLimit;
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

/**
 * 订单接口控制器
 * 
 * 作用：
 * - 提供订单相关的API接口
 * - 包括订单创建、查询、取消、支付等功能
 * - 区分用户端和管理端接口
 * 
 * 说明：
 * - 用户端接口：需要登录，只能操作自己的订单
 * - 管理端接口：需要管理员权限，可以操作所有订单
 */
@RestController
/**
 * @RestController 注解：
 * - 标识这是一个REST控制器
 * - 方法返回值自动转换为JSON
 */
@RequestMapping("/api/order")
/**
 * @RequestMapping 注解：
 * - 定义了该控制器类中所有接口的统一根路径为/api/order
 * - 所有方法的URL都会以/api/order开头
 */
public class OrderController {

    @Autowired
    /**
     * @Autowired 注解说明：
     * 
     * OrderController 就能直接调用 OrderService 中实现的业务逻辑方法
     * 而不需要手动去 new OrderServiceImpl()
     * 
     * Spring会自动从容器中找到OrderService的实现类并注入
     */
    private OrderService orderService;

    // --------------------------------------------------------------------------
    // --- 用户端接口 (依赖 URL 参数: userId) ---
    // --------------------------------------------------------------------------

    /**
     * 1. 预订票务（抢票）
     * 
     * 请求路径: POST /api/order/create
     * 
     * 功能：
     * - 用户创建订单（下单）
     * - 需要登录（userId从JWT Token中获取）
     * - 自动减少演出库存
     * - 限流：每分钟最多5次请求
     * 
     * URL 参数：
     * - showId: 演出ID（必填）
     * - quantity: 购买数量（必填）
     * 
     * userId从JWT Token中获取（由JwtAuthenticationFilter设置）
     * 
     * @param showId 演出ID
     * @param quantity 购买数量
     * @param request HTTP请求对象，包含userId属性
     * @return ResponseEntity对象，包含订单信息
     *         如果订单创建成功，返回 {"order": {订单详情JSON对象}}
     *         如果失败，返回 {"order": null}
     */
    @PostMapping("/create")
    @RateLimit(maxRequests = 5, timeWindow = 60, keyPrefix = "rate:order") // 每分钟最多5次
    public ResponseEntity<Map<String, Object>> createOrder(@RequestParam Long showId,
                                                            @RequestParam Integer quantity,
                                                            HttpServletRequest request) {
        /**
         * 从Request属性中获取userId（由JwtAuthenticationFilter设置）
         */
        Long userId = (Long) request.getAttribute("userId");
        Order order = null;
        if (userId != null) {
            /**
             * 调用Service层创建订单
             */
            order = orderService.createOrder(userId, showId, quantity);
        }
        /**
         * 使用Map包装，确保即使order为null也返回有效的JSON对象
         */
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        return ResponseEntity.ok(result);
    }

    /**
     * 2. 查询订单详情
     * 
     * 请求路径: GET /api/order/details
     * 
     * 功能：
     * - 用户查询自己的订单详情
     * - 需要登录
     * 
     * URL 参数：
     * - orderId: 订单ID（必填）
     * 
     * userId从JWT Token中获取
     * 
     * @param orderId 订单ID
     * @param request HTTP请求对象，包含userId属性
     * @return ResponseEntity对象，包含订单信息
     *         如果订单存在，返回 {"order": {订单详情JSON对象}}
     *         如果不存在，返回 {"order": null}
     */
    @GetMapping("/details")
    public ResponseEntity<Map<String, Object>> getOrderDetails(@RequestParam Long orderId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Order order = null;
        if (userId != null) {
            /**
             * 调用Service层查询订单详情（会验证订单属于该用户）
             */
            order = orderService.getOrderDetails(orderId, userId);
        }
        /**
         * 使用Map包装，确保即使order为null也返回有效的JSON对象
         */
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        return ResponseEntity.ok(result);
    }

    /**
     * 3. 取消订单
     * 
     * 请求路径: PUT /api/order/cancel
     * 
     * 功能：
     * - 用户取消自己的订单
     * - 需要登录
     * - 取消后恢复演出库存
     * 
     * URL 参数：
     * - orderId: 订单ID（必填）
     * 
     * userId从JWT Token中获取
     * 
     * @param orderId 订单ID
     * @param request HTTP请求对象，包含userId属性
     * @return ApiResponse对象，包含操作结果
     */
    @PutMapping("/cancel")
    public ApiResponse cancelOrder(@RequestParam Long orderId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.failure("取消失败：请先登录。");
        }

        /**
         * 调用Service层取消订单（会验证订单属于该用户）
         */
        boolean success = orderService.cancelOrder(orderId, userId);
        if (success) {
            return ApiResponse.success("订单取消成功。库存已返还。");
        } else {
            return ApiResponse.failure("取消失败！订单不存在、不属于您、或状态不可取消。");
        }
    }

    /**
     * 4. 支付订单
     * 
     * 请求路径: PUT /api/order/pay
     * 
     * 功能：
     * - 用户支付订单（简化版，直接更新订单状态）
     * - 需要登录
     * - 实际项目中应该调用支付接口
     * 
     * URL 参数：
     * - orderId: 订单ID（必填）
     * 
     * userId从JWT Token中获取
     * 
     * @param orderId 订单ID
     * @param request HTTP请求对象，包含userId属性
     * @return ApiResponse对象，包含操作结果
     */
    @PutMapping("/pay")
    public ApiResponse payOrder(@RequestParam Long orderId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.failure("支付失败：请先登录。");
        }

        /**
         * 调用Service层支付订单（会验证订单属于该用户）
         */
        boolean success = orderService.payOrder(orderId, userId);
        if (success) {
            return ApiResponse.success("订单支付成功！");
        } else {
            return ApiResponse.failure("支付失败！订单不存在、不属于您、或状态不可支付。");
        }
    }

    /**
     * 5. 分页查询指定用户的订单列表
     * 
     * 请求路径: GET /api/order/list
     * 
     * 功能：
     * - 用户查询自己的订单列表
     * - 需要登录
     * - 支持分页
     * 
     * URL 参数：
     * - pageNum: 当前页码（默认1）
     * - pageSize: 每页数量（默认10）
     * 
     * userId从JWT Token中获取
     * 
     * @param pageNum 当前页码
     * @param pageSize 每页数量
     * @param request HTTP请求对象，包含userId属性
     * @return PageResult对象，包含订单列表和分页信息
     */
    @GetMapping("/list")
    public PageResult<Order> getUserOrderList(@RequestParam(defaultValue = "1") int pageNum,
                                               @RequestParam(defaultValue = "10") int pageSize,
                                               HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            /**
             * 未登录，返回空的分页结果，而不是null
             */
            return PageResult.build(0, pageNum, pageSize, List.of());
        }
        /**
         * 调用Service层查询用户订单列表
         */
        return orderService.getUserOrderList(userId, pageNum, pageSize);
    }

    /**
     * 用户端 - 条件查询订单列表（分页）
     * 
     * 请求路径: GET /api/order/query
     * 
     * 功能：
     * - 用户查询自己的订单列表，支持按状态筛选
     * - 需要登录
     * - 支持分页
     * 
     * URL 参数：
     * - status: 订单状态（可选），1=待支付，2=已支付，3=已取消，4=已退款
     * - pageNum: 当前页码（默认1）
     * - pageSize: 每页数量（默认10）
     * 
     * userId从JWT Token中获取
     * 
     * @param status 订单状态（可选）
     * @param pageNum 当前页码
     * @param pageSize 每页数量
     * @param request HTTP请求对象，包含userId属性
     * @return PageResult对象，包含订单列表和分页信息
     */
    @GetMapping("/query")
    public PageResult<Order> getUserOrderListWithConditions(@RequestParam(required = false) Integer status,
                                                             @RequestParam(defaultValue = "1") int pageNum,
                                                             @RequestParam(defaultValue = "10") int pageSize,
                                                             HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            /**
             * 未登录，返回空的分页结果，而不是null
             */
            return PageResult.build(0, pageNum, pageSize, List.of());
        }
        /**
         * 调用Service层条件查询订单列表
         */
        return orderService.getUserOrderListWithConditions(userId, status, pageNum, pageSize);
    }

    // --------------------------------------------------------------------------
    // --- 管理端接口 (依赖 URL 参数: orderId) ---
    // --------------------------------------------------------------------------

    /**
     * 6. 管理端 - 查询所有订单列表
     * 
     * 请求路径: GET /api/order/admin/all
     * 
     * 功能：
     * - 管理员查询所有订单
     * - 需要管理员权限
     * - 支持分页
     * 
     * @param pageNum 当前页码（默认1）
     * @param pageSize 每页数量（默认10）
     * @return PageResult对象，包含订单列表和分页信息
     */
    @GetMapping("/admin/all")
    public PageResult<Order> getAllOrderList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        /**
         * 调用Service层查询所有订单列表
         */
        return orderService.getAllOrderList(pageNum, pageSize);
    }

    /**
     * 管理端 - 条件查询订单列表（分页）
     * 
     * 请求路径: GET /api/order/admin/query
     * 
     * 功能：
     * - 管理员查询订单，支持多条件筛选
     * - 需要管理员权限
     * - 支持分页
     * 
     * @param userId 用户ID（可选），如果为null则查询所有用户
     * @param status 订单状态（可选），1=待支付，2=已支付，3=已取消，4=已退款
     * @param pageNum 当前页码（默认1）
     * @param pageSize 每页数量（默认10）
     * @return PageResult对象，包含订单列表和分页信息
     */
    @GetMapping("/admin/query")
    public PageResult<Order> getAllOrderListWithConditions(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        /**
         * 调用Service层条件查询订单列表
         */
        return orderService.getAllOrderListWithConditions(userId, status, pageNum, pageSize);
    }

    /**
     * 7. 管理端 - 手动更新订单状态
     * 
     * 请求路径: PUT /api/order/admin/status
     * 
     * 功能：
     * - 管理员手动修改订单状态
     * - 用于处理异常订单（如手动退款、强制取消等）
     * - 需要管理员权限
     * 
     * URL 参数：
     * - orderId: 订单ID（必填）
     * - newStatus: 新状态（必填），1=待支付，2=已支付，3=已取消，4=已退款
     * 
     * 说明：
     * - 该SQL只接收orderId和newStatus，不检查userId和expectedStatus
     * - 管理员有更高权限，可以强制更新订单状态
     * 
     * @param orderId 订单ID
     * @param newStatus 新状态
     * @return 结果信息（字符串）
     */
    @PutMapping("/admin/status")
    public String updateOrderStatus(
            @RequestParam Long orderId,
            @RequestParam Integer newStatus) {

        /**
         * 调用Service层更新订单状态（管理端版本，不检查旧状态）
         */
        boolean success = orderService.updateOrderStatus(orderId, newStatus);

        if (success) {
            return String.format("订单 ID: %d 状态已更新为: %d。", orderId, newStatus);
        } else {
            return String.format("更新失败！订单 ID: %d 不存在或操作失败。", orderId);
        }
    }
}
