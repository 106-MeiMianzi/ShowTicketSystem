package com.mianzi.showticketsystem.controller;

import com.mianzi.showticketsystem.model.dto.ApiResponse;
import com.mianzi.showticketsystem.model.dto.LoginResponse;
import com.mianzi.showticketsystem.model.entity.Order;
import com.mianzi.showticketsystem.model.entity.PageResult;
import com.mianzi.showticketsystem.model.entity.Show;
import com.mianzi.showticketsystem.model.entity.User;
import com.mianzi.showticketsystem.service.OrderService;
import com.mianzi.showticketsystem.service.ShowService;
import com.mianzi.showticketsystem.service.UserService;
import com.mianzi.showticketsystem.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理端接口控制器
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private ShowService showService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 管理端 - 登录
     * 请求路径: POST /api/admin/login
     */
    @PostMapping("/login")
    public LoginResponse login(@RequestParam String account, @RequestParam String password) {
        User user = userService.login(account, password);
        if (user != null && user.getIsAdmin()) {
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
            return new LoginResponse(token, user.getId(), user.getUsername(), user.getRole(),
                    "管理员登录成功！欢迎，" + user.getUsername());
        } else {
            return new LoginResponse(null, null, null, null, "登录失败：账号或密码错误，或您不是管理员。");
        }
    }

    /**
     * 管理端 - 退出登录
     * 请求路径: POST /api/admin/logout
     * 注意：JWT是无状态的，客户端删除token即可，服务端不需要特殊处理
     */
    @PostMapping("/logout")
    public ApiResponse logout() {
        return ApiResponse.success("退出登录成功！请客户端删除本地存储的token。");
    }

    // ==================== 用户管理 ====================

    /**
     * 管理端 - 分页查询用户列表（条件查询）
     * 请求路径: GET /api/admin/users
     */
    @GetMapping("/users")
    public PageResult<User> getUserList(@RequestParam(required = false) String username,
                                        @RequestParam(required = false) Integer status,
                                        @RequestParam(defaultValue = "1") int pageNum,
                                        @RequestParam(defaultValue = "10") int pageSize) {
        return userService.getUserList(username, status, pageNum, pageSize);
    }

    /**
     * 管理端 - 查询用户详情
     * 请求路径: GET /api/admin/users/{id}
     * @return 如果用户存在，返回 {"user": {用户详情JSON对象}}；如果不存在，返回 {"user": null}
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> getUserDetail(@PathVariable Long id) {
        User user = userService.getUserById(id);
        // 使用 Map 包装，确保即使 user 为 null 也返回有效的 JSON 对象
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        return ResponseEntity.ok(result);
    }

    /**
     * 管理端 - 修改用户信息
     * 请求路径: PUT /api/admin/users/{id}
     */
    @PutMapping("/users/{id}")
    public ApiResponse updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        boolean success = userService.updateUser(user);
        if (success) {
            return ApiResponse.success("用户信息更新成功！");
        } else {
            return ApiResponse.failure("更新失败！");
        }
    }

    /**
     * 管理端 - 启用/禁用用户
     * 请求路径: PUT /api/admin/users/{id}/status
     * @param status 用户状态（0:禁用, 1:正常）
     */
    @PutMapping("/users/{id}/status")
    public ApiResponse updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        // 验证状态值是否有效（只允许 0 或 1）
        if (status == null || (status != 0 && status != 1)) {
            return ApiResponse.failure("更新失败！状态值无效，只允许 0（禁用）或 1（正常）。");
        }
        
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        boolean success = userService.updateUser(user);
        if (success) {
            return ApiResponse.success("用户状态更新成功！");
        } else {
            return ApiResponse.failure("更新失败！");
        }
    }

    // ==================== 演出管理 ====================

    /**
     * 管理端 - 添加演出信息
     * 请求路径: POST /api/admin/shows
     */
    @PostMapping("/shows")
    public ApiResponse addShow(@RequestBody Show show) {
        // 验证名称为空
        if (show.getName() == null || show.getName().trim().isEmpty()) {
            return ApiResponse.failure("添加失败！");
        }

        // 验证总票数不能为0或负数
        if (show.getTotalTickets() == null || show.getTotalTickets() <= 0) {
            return ApiResponse.failure("添加失败！");
        }

        // 验证价格不能为null或负数
        if (show.getPrice() == null || show.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            return ApiResponse.failure("添加失败！");
        }

        boolean success = showService.publishShow(show);
        if (success) {
            return ApiResponse.success("演出添加成功！ID: " + show.getId());
        } else {
            return ApiResponse.failure("添加失败！");
        }
    }

    /**
     * 管理端 - 修改演出信息
     * 请求路径: PUT /api/admin/shows/{id}
     */
    @PutMapping("/shows/{id}")
    public ApiResponse updateShow(@PathVariable Long id, @RequestBody Show show) {
        show.setId(id);
        boolean success = showService.updateShow(show);
        if (success) {
            return ApiResponse.success("演出信息更新成功！");
        } else {
            return ApiResponse.failure("更新失败！");
        }
    }

    /**
     * 管理端 - 分页查询演出列表（条件查询）
     * 请求路径: GET /api/admin/shows
     */
    @GetMapping("/shows")
    public PageResult<Show> getShowList(@RequestParam(required = false) String name,
                                        @RequestParam(required = false) String region,
                                        @RequestParam(required = false) String category,
                                        @RequestParam(required = false) Integer status,
                                        @RequestParam(defaultValue = "1") int pageNum,
                                        @RequestParam(defaultValue = "10") int pageSize) {
        return showService.getShowListForAdmin(name, region, category, status, pageNum, pageSize);
    }

    /**
     * 管理端 - 查询演出信息详情（此时库存需要明确数字）
     * 请求路径: GET /api/admin/shows/{id}
     * @return 如果演出存在，返回 {"show": {演出详情JSON对象}}；如果不存在，返回 {"show": null}
     */
    @GetMapping("/shows/{id}")
    public ResponseEntity<Map<String, Object>> getShowDetail(@PathVariable Long id) {
        Show show = showService.getShowByIdForAdmin(id);
        // 使用 Map 包装，确保即使 show 为 null 也返回有效的 JSON 对象
        Map<String, Object> result = new HashMap<>();
        result.put("show", show);
        return ResponseEntity.ok(result);
    }

    /**
     * 管理端 - 删除演出信息
     * 请求路径: DELETE /api/admin/shows/{id}
     */
    @DeleteMapping("/shows/{id}")
    public ApiResponse deleteShow(@PathVariable Long id) {
        boolean success = showService.deleteShow(id);
        if (success) {
            return ApiResponse.success("演出信息删除成功！");
        } else {
            return ApiResponse.failure("删除失败！");
        }
    }

    // ==================== 订单管理 ====================

    /**
     * 管理端 - 分页查询订单列表（条件查询）
     * 请求路径: GET /api/admin/orders
     */
    @GetMapping("/orders")
    public PageResult<Order> getOrderList(@RequestParam(required = false) Long userId,
                                          @RequestParam(required = false) Integer status,
                                          @RequestParam(defaultValue = "1") int pageNum,
                                          @RequestParam(defaultValue = "10") int pageSize) {
        return orderService.getAllOrderListWithConditions(userId, status, pageNum, pageSize);
    }

    /**
     * 管理端 - 查询订单详情
     * 请求路径: GET /api/admin/orders/{id}
     * @return 如果订单存在，返回 {"order": {订单详情JSON对象}}；如果不存在，返回 {"order": null}
     */
    @GetMapping("/orders/{id}")
    public ResponseEntity<Map<String, Object>> getOrderDetail(@PathVariable Long id) {
        // 管理端可以查询所有订单，userId传null表示管理员查询
        Order order = orderService.getOrderDetails(id, null);
        // 使用 Map 包装，确保即使 order 为 null 也返回有效的 JSON 对象
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        return ResponseEntity.ok(result);
    }

    /**
     * 管理端 - 修改订单信息（手动更新订单状态）
     * 请求路径: PUT /api/admin/orders/{id}/status
     * @param newStatus 订单状态（1:待支付, 2:已支付, 3:已取消, 4:已退款）
     */
    @PutMapping("/orders/{id}/status")
    public ApiResponse updateOrderStatus(@PathVariable Long id, @RequestParam Integer newStatus) {
        // 验证状态值是否有效（只允许 1、2、3、4）
        if (newStatus == null || (newStatus < 1 || newStatus > 4)) {
            return ApiResponse.failure("更新失败！状态值无效，只允许 1（待支付）、2（已支付）、3（已取消）、4（已退款）。");
        }
        
        boolean success = orderService.updateOrderStatus(id, newStatus);
        if (success) {
            return ApiResponse.success("订单状态更新成功！");
        } else {
            return ApiResponse.failure("更新失败！");
        }
    }
}

