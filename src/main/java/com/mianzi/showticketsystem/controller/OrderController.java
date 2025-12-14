package com.mianzi.showticketsystem.controller;

import com.mianzi.showticketsystem.model.entity.Order;
import com.mianzi.showticketsystem.model.entity.PageResult;
import com.mianzi.showticketsystem.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
     * URL 参数：showId, quantity, userId
     */
    //标记这个 Java 方法处理的是 POST 请求
    //子路径定义
    @PostMapping("/create")
    public Order createOrder(
            //参数获取方式
            //方法所需的参数（showId、quantity、userId）应该从 HTTP 请求的 查询参数（Query Parameters）
            //或 表单数据（Form Data） 中获取
            @RequestParam Long showId,
            @RequestParam Integer quantity,
            @RequestParam Long userId) {

        //响应数据类型
        //因为 Controller 类上有 @RestController 注解
        //这个 Order 对象会被 Spring Boot 自动转换为 JSON 格式
        //作为 HTTP 响应体发送给客户端
        return orderService.createOrder(userId, showId, quantity);
    }

    /**
     * 2. 查询订单详情
     * URL 参数：orderId, userId
     */
    //GET 请求
    //子路径
    @GetMapping("/details")
    public Order getOrderDetails(
            //获取订单ID
            @RequestParam Long orderId,
            //获取用户ID (用于权限校验)
            @RequestParam Long userId) {

        // Service 层需要 userId 进行权限校验（只能查自己的订单）
        //1. 查询订单
        //2. 验证 userId 是否匹配该订单。
        return orderService.getOrderDetails(orderId, userId);
    }

    /**
     * 3. 取消订单
     * URL 参数：orderId, userId
     */
    //子路径
    @PutMapping("/cancel")
    public String cancelOrder(
            @RequestParam Long orderId,
            @RequestParam Long userId) {

        boolean success = orderService.cancelOrder(orderId, userId);

        //PUT 请求:通常用于 修改/更新 资源状态
        //根据 Service 层返回的布尔值判断操作结果
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
    //返回一个封装了分页信息的对象
    //用于封装订单列表数据、总记录数、总页数、当前页码等分页信息，方便前端展示
    public PageResult<Order> getUserOrderList(
            @RequestParam Long userId,
            //默认值配置
            //如果客户端请求中没有提供 pageNum，它将默认使用 1
            //pageSize 同理
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
    //属于管理员路径 /admin/all，与用户端接口隔离
    @GetMapping("/admin/all")
    public PageResult<Order> getAllOrderList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        //不传入 userId，Service 层来查询所有用户的订单数据
        return orderService.getAllOrderList(pageNum, pageSize);
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
