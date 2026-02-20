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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理端接口控制器
 * 
 * 作用：
 * - 提供管理员专用的API接口
 * - 管理用户、演出、订单等资源
 * - 需要管理员权限才能访问（由JwtAuthenticationFilter验证）
 * 
 * 权限说明：
 * - 所有接口都需要管理员权限（role = 2）
 * - JwtAuthenticationFilter会自动验证用户角色
 * - 非管理员访问会返回403禁止访问
 */
@RestController
/**
 * @RestController 注解：
 * - 标识这是一个REST控制器
 * - 方法返回值自动转换为JSON
 */
@RequestMapping("/api/admin")
/**
 * @RequestMapping 注解：
 * - 定义控制器的基础路径为/api/admin
 * - 所有方法的URL都会以/api/admin开头
 */
public class AdminController {

    /**
     * 注入用户服务
     * 
     * 用于管理用户相关的业务逻辑
     */
    @Autowired
    private UserService userService;

    /**
     * 注入演出服务
     * 
     * 用于管理演出相关的业务逻辑
     */
    @Autowired
    private ShowService showService;

    /**
     * 注入订单服务
     * 
     * 用于管理订单相关的业务逻辑
     */
    @Autowired
    private OrderService orderService;

    /**
     * 注入JWT工具类
     * 
     * 用于生成JWT Token（管理员登录时）
     */
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 管理端 - 登录
     * 
     * 请求路径: POST /api/admin/login
     * 
     * 说明：
     * - 管理员登录接口，不需要Token验证（在排除列表中）
     * - 登录成功后返回JWT Token
     * 
     * @param account 账号（用户名或邮箱）
     * @param password 密码
     * @return 成功 200 + LoginResponse；认证失败 401
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestParam String account, @RequestParam String password) {
        /**
         * 调用Service层登录方法
         */
        User user = userService.login(account, password);
        
        /**
         * 验证用户是否存在且是管理员
         */
        if (user != null && user.getIsAdmin()) {
            /**
             * 登录成功，生成JWT Token
             */
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
            return ResponseEntity.ok(new LoginResponse(token, user.getId(), user.getUsername(), user.getRole(),
                    "管理员登录成功！欢迎，" + user.getUsername()));
        } else {
            /**
             * 登录失败：账号或密码错误，或不是管理员 → 401
             */
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new LoginResponse(null, null, null, null, "登录失败：账号或密码错误，或您不是管理员。"));
        }
    }

    /**
     * 管理端 - 退出登录
     * 
     * 请求路径: POST /api/admin/logout
     * 
     * 说明：
     * - JWT是无状态的，客户端删除token即可
     * - 服务端不需要特殊处理
     * 
     * @return ApiResponse对象，包含成功消息
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout() {
        return ResponseEntity.ok(ApiResponse.success("退出登录成功！请客户端删除本地存储的token。"));
    }

    // ==================== 用户管理 ====================

    /**
     * 管理端 - 分页查询用户列表（条件查询）
     * 
     * 请求路径: GET /api/admin/users
     * 
     * 功能：
     * - 支持按用户名模糊查询
     * - 支持按状态筛选（正常/禁用）
     * - 支持分页查询
     * 
     * @param username 用户名（可选），用于模糊查询
     * @param status 状态（可选），1=正常，0=禁用
     * @param pageNum 当前页码（默认1）
     * @param pageSize 每页数量（默认10）
     * @return PageResult对象，包含用户列表和分页信息
     */
    @GetMapping("/users")
    public PageResult<User> getUserList(@RequestParam(required = false) String username,
                                        @RequestParam(required = false) Integer status,
                                        @RequestParam(defaultValue = "1") int pageNum,
                                        @RequestParam(defaultValue = "10") int pageSize) {
        /**
         * 调用Service层方法查询用户列表
         */
        return userService.getUserList(username, status, pageNum, pageSize);
    }

    /**
     * 管理端 - 查询用户详情
     * 
     * 请求路径: GET /api/admin/users/{id}
     * 
     * @param id 用户ID（路径变量）
     * @return ResponseEntity对象，包含用户信息
     *         如果用户存在，返回 {"user": {用户详情JSON对象}}
     *         如果不存在，返回 {"user": null}
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> getUserDetail(@PathVariable Long id) {
        /**
         * 查询用户信息
         */
        User user = userService.getUserById(id);
        
        /**
         * 使用Map包装，确保即使user为null也返回有效的JSON对象
         */
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        return ResponseEntity.ok(result);
    }

    /**
     * 管理端 - 修改用户信息
     * 
     * 请求路径: PUT /api/admin/users/{id}
     * 
     * 功能：
     * - 管理员可以修改任意用户的信息
     * - 可以修改用户名、邮箱、手机号、真实姓名等
     * 
     * @param id 用户ID（路径变量）
     * @param user 用户对象，包含要更新的字段（JSON格式）
     * @return ApiResponse对象，包含操作结果
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse> updateUser(@PathVariable Long id, @RequestBody User user) {
        /**
         * 设置用户ID（确保更新的是指定用户）
         */
        user.setId(id);
        
        /**
         * 调用Service层更新用户信息
         */
        boolean success = userService.updateUser(user);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("用户信息更新成功！"));
        } else {
            return ApiResponse.failureBadRequest("更新失败！");
        }
    }

    /**
     * 管理端 - 启用/禁用用户
     * 
     * 请求路径: PUT /api/admin/users/{id}/status
     * 
     * 功能：
     * - 管理员可以启用或禁用用户账号
     * - 禁用的用户无法登录
     * 
     * @param id 用户ID（路径变量）
     * @param status 用户状态（0:禁用, 1:正常）
     * @return ApiResponse对象，包含操作结果
     */
    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        /**
         * 步骤1：验证状态值是否有效（只允许0或1）
         */
        if (status == null || (status != 0 && status != 1)) {
            return ApiResponse.failureBadRequest("更新失败！状态值无效，只允许 0（禁用）或 1（正常）。");
        }
        
        /**
         * 步骤2：创建User对象并设置ID和状态
         */
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        
        /**
         * 步骤3：调用Service层更新用户状态
         */
        boolean success = userService.updateUser(user);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("用户状态更新成功！"));
        } else {
            return ApiResponse.failureBadRequest("更新失败！");
        }
    }

    // ==================== 演出管理 ====================

    /**
     * 管理端 - 添加演出信息
     * 
     * 请求路径: POST /api/admin/shows
     * 
     * 功能：
     * - 管理员发布新演出
     * - 需要验证必填字段和格式
     * 
     * @param show 演出对象，包含演出信息（JSON格式）
     * @return ApiResponse对象，包含操作结果
     */
    @PostMapping("/shows")
    public ResponseEntity<ApiResponse> addShow(@RequestBody Show show) {
        /**
         * 步骤1：验证名称为空
         */
        if (show.getName() == null || show.getName().trim().isEmpty()) {
            return ApiResponse.failureBadRequest("添加失败！");
        }

        /**
         * 步骤2：验证总票数不能为0或负数
         */
        if (show.getTotalTickets() == null || show.getTotalTickets() <= 0) {
            return ApiResponse.failureBadRequest("添加失败！");
        }

        /**
         * 步骤3：验证价格不能为null或负数
         * 
         * BigDecimal比较：
         * - compareTo()返回负数表示小于，0表示等于，正数表示大于
         * - compareTo(ZERO) <= 0 表示小于等于0
         */
        if (show.getPrice() == null || show.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            return ApiResponse.failureBadRequest("添加失败！");
        }

        /**
         * 步骤4：调用Service层发布演出
         */
        boolean success = showService.publishShow(show);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("演出添加成功！ID: " + show.getId()));
        } else {
            return ApiResponse.failureBadRequest("添加失败！");
        }
    }

    /**
     * 管理端 - 修改演出信息
     * 
     * 请求路径: PUT /api/admin/shows/{id}
     * 
     * @param id 演出ID（路径变量）
     * @param show 演出对象，包含要更新的字段（JSON格式）
     * @return ApiResponse对象，包含操作结果
     */
    @PutMapping("/shows/{id}")
    public ResponseEntity<ApiResponse> updateShow(@PathVariable Long id, @RequestBody Show show) {
        /**
         * 设置演出ID（确保更新的是指定演出）
         */
        show.setId(id);
        
        /**
         * 调用Service层更新演出信息
         */
        boolean success = showService.updateShow(show);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("演出信息更新成功！"));
        } else {
            return ApiResponse.failureBadRequest("更新失败！");
        }
    }

    /**
     * 管理端 - 分页查询演出列表（条件查询）
     * 
     * 请求路径: GET /api/admin/shows
     * 
     * 功能：
     * - 支持按演出名称模糊查询
     * - 支持按地区、分类、状态筛选
     * - 支持分页查询
     * 
     * @param name 演出名称（可选），用于模糊查询
     * @param region 地区（可选）
     * @param category 分类（可选）
     * @param status 状态（可选），1=正常，0=已取消
     * @param pageNum 当前页码（默认1）
     * @param pageSize 每页数量（默认10）
     * @return PageResult对象，包含演出列表和分页信息
     */
    @GetMapping("/shows")
    public PageResult<Show> getShowList(@RequestParam(required = false) String name,
                                        @RequestParam(required = false) String region,
                                        @RequestParam(required = false) String category,
                                        @RequestParam(required = false) Integer status,
                                        @RequestParam(defaultValue = "1") int pageNum,
                                        @RequestParam(defaultValue = "10") int pageSize) {
        /**
         * 调用Service层方法查询演出列表
         */
        return showService.getShowListForAdmin(name, region, category, status, pageNum, pageSize);
    }

    /**
     * 管理端 - 查询演出信息详情（此时库存需要明确数字）
     * 
     * 请求路径: GET /api/admin/shows/{id}
     * 
     * 说明：
     * - 管理端可以查看所有状态的演出（包括已取消的）
     * - 显示明确的库存数字
     * 
     * @param id 演出ID（路径变量）
     * @return ResponseEntity对象，包含演出信息
     *         如果演出存在，返回 {"show": {演出详情JSON对象}}
     *         如果不存在，返回 {"show": null}
     */
    @GetMapping("/shows/{id}")
    public ResponseEntity<Map<String, Object>> getShowDetail(@PathVariable Long id) {
        /**
         * 调用Service层方法查询演出详情（管理端版本）
         */
        Show show = showService.getShowByIdForAdmin(id);
        
        /**
         * 使用Map包装，确保即使show为null也返回有效的JSON对象
         */
        Map<String, Object> result = new HashMap<>();
        result.put("show", show);
        return ResponseEntity.ok(result);
    }

    /**
     * 管理端 - 删除演出信息
     * 
     * 请求路径: DELETE /api/admin/shows/{id}
     * 
     * 说明：
     * - 物理删除演出记录
     * - 删除前需要检查是否有关联订单
     * 
     * @param id 演出ID（路径变量）
     * @return ApiResponse对象，包含操作结果
     */
    @DeleteMapping("/shows/{id}")
    public ResponseEntity<ApiResponse> deleteShow(@PathVariable Long id) {
        /**
         * 调用Service层删除演出
         */
        boolean success = showService.deleteShow(id);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("演出信息删除成功！"));
        } else {
            return ApiResponse.failureBadRequest("删除失败！");
        }
    }

    // ==================== 订单管理 ====================

    /**
     * 管理端 - 分页查询订单列表（条件查询）
     * 
     * 请求路径: GET /api/admin/orders
     * 
     * 功能：
     * - 支持按用户ID筛选
     * - 支持按订单状态筛选
     * - 支持分页查询
     * 
     * @param userId 用户ID（可选），如果为null则查询所有用户
     * @param status 订单状态（可选），1=待支付，2=已支付，3=已取消，4=已退款
     * @param pageNum 当前页码（默认1）
     * @param pageSize 每页数量（默认10）
     * @return PageResult对象，包含订单列表和分页信息
     */
    @GetMapping("/orders")
    public PageResult<Order> getOrderList(@RequestParam(required = false) Long userId,
                                          @RequestParam(required = false) Integer status,
                                          @RequestParam(defaultValue = "1") int pageNum,
                                          @RequestParam(defaultValue = "10") int pageSize) {
        /**
         * 调用Service层方法查询订单列表
         */
        return orderService.getAllOrderListWithConditions(userId, status, pageNum, pageSize);
    }

    /**
     * 管理端 - 查询订单详情
     * 
     * 请求路径: GET /api/admin/orders/{id}
     * 
     * 说明：
     * - 管理端可以查询所有订单，不需要验证用户ID
     * 
     * @param id 订单ID（路径变量）
     * @return ResponseEntity对象，包含订单信息
     *         如果订单存在，返回 {"order": {订单详情JSON对象}}
     *         如果不存在，返回 {"order": null}
     */
    @GetMapping("/orders/{id}")
    public ResponseEntity<Map<String, Object>> getOrderDetail(@PathVariable Long id) {
        /**
         * 管理端可以查询所有订单，userId传null表示管理员查询
         */
        Order order = orderService.getOrderDetails(id, null);
        
        /**
         * 使用Map包装，确保即使order为null也返回有效的JSON对象
         */
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        return ResponseEntity.ok(result);
    }

    /**
     * 管理端 - 修改订单信息（手动更新订单状态）
     * 
     * 请求路径: PUT /api/admin/orders/{id}/status
     * 
     * 功能：
     * - 管理员可以手动修改订单状态
     * - 用于处理异常订单（如手动退款、强制取消等）
     * 
     * @param id 订单ID（路径变量）
     * @param newStatus 订单状态（1:待支付, 2:已支付, 3:已取消, 4:已退款）
     * @return ApiResponse对象，包含操作结果
     */
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<ApiResponse> updateOrderStatus(@PathVariable Long id, @RequestParam Integer newStatus) {
        /**
         * 步骤1：验证状态值是否有效（只允许1、2、3、4）
         */
        if (newStatus == null || (newStatus < 1 || newStatus > 4)) {
            return ApiResponse.failureBadRequest("更新失败！状态值无效，只允许 1（待支付）、2（已支付）、3（已取消）、4（已退款）。");
        }
        
        /**
         * 步骤2：调用Service层更新订单状态
         * 
         * 管理端可以强制更新订单状态，不检查旧状态
         */
        boolean success = orderService.updateOrderStatus(id, newStatus);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("订单状态更新成功！"));
        } else {
            return ApiResponse.failureBadRequest("更新失败！");
        }
    }
}
