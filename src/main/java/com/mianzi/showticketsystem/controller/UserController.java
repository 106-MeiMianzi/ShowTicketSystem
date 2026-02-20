package com.mianzi.showticketsystem.controller;

import com.mianzi.showticketsystem.model.dto.ApiResponse;
import com.mianzi.showticketsystem.model.dto.LoginResponse;
import com.mianzi.showticketsystem.model.dto.RegisterOrLoginRequest;
import com.mianzi.showticketsystem.model.entity.Address;
import com.mianzi.showticketsystem.model.entity.User;
import com.mianzi.showticketsystem.service.AddressService;
import com.mianzi.showticketsystem.service.UserService;
import com.mianzi.showticketsystem.util.JwtUtil;
import com.mianzi.showticketsystem.util.UsernameValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户接口控制器
 * 
 * 作用：
 * - 接收来自前端的HTTP请求
 * - 调用Service层（UserService、AddressService）的业务逻辑
 * - 返回JSON格式的响应数据
 * 
 * 职责：
 * - 参数验证和格式化
 * - 调用Service层方法
 * - 处理HTTP响应
 * - 生成JWT Token（登录时）
 */
@RestController
/**
 * @RestController 注解说明：
 * 
 * Spring MVC提供的组合注解，等价于@Controller + @ResponseBody
 * 
 * 作用：
 * - @Controller：标识这是一个控制器类，处理HTTP请求
 * - @ResponseBody：方法返回值直接写入HTTP响应体，不返回视图
 * - 所有方法的返回值都会自动转换为JSON格式
 */
@RequestMapping("/api/user")
/**
 * @RequestMapping 注解说明：
 * 
 * 定义控制器的基础路径
 * 
 * 作用：
 * - 所有方法的URL都会以/api/user开头
 * - 例如：@GetMapping("/current") 的完整路径是 /api/user/current
 */
public class UserController {

    /**
     * 注入用户服务
     * 
     * @Autowired 注解：
     * - 自动注入UserService的实现类（UserServiceImpl）
     * - Spring会自动从容器中找到UserService并注入
     */
    @Autowired
    private UserService userService;

    /**
     * 注入地址服务
     * 
     * 用于处理收货地址相关的业务逻辑
     */
    @Autowired
    private AddressService addressService;

    /**
     * 注入JWT工具类
     * 
     * 用于生成和解析JWT Token
     */
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户注册/登录接口（合并接口）
     * 
     * 功能说明：
     * - 如果用户不存在则注册，存在则登录
     * - 支持用户名和邮箱登录
     * - 自动判断是注册还是登录
     * 
     * 请求路径: POST /api/user/register-or-login
     * 
     * 请求体（JSON格式）：
     * {
     *   "username": "用户名或邮箱（必填）",
     *   "password": "密码（必填）",
     *   "email": "邮箱（可选，注册时建议提供）"
     * }
     * 
     * Content-Type: application/json
     * 
     * 返回：成功时 200 + LoginResponse；参数/校验失败 400；认证失败（密码错误等）401
     * 
     * @param request 注册/登录请求对象，包含username、password、email字段
     * @return ResponseEntity&lt;LoginResponse&gt;，含合适的状态码
     */
    @PostMapping("/register-or-login")
    /**
     * @PostMapping 注解：
     * - 处理POST请求
     * - 等价于@RequestMapping(method = RequestMethod.POST)
     */
    public ResponseEntity<LoginResponse> registerOrLogin(@RequestBody RegisterOrLoginRequest request) {
        /**
         * @RequestBody 注解说明：
         * - 从HTTP请求体中获取JSON数据并自动转换为Java对象
         * - Spring会自动将JSON格式的请求体反序列化为RegisterOrLoginRequest对象
         * - 前端需要设置Content-Type为application/json
         */
        
        /**
         * 从请求对象中提取参数
         */
        String username = request.getUsername();
        String password = request.getPassword();
        String email = request.getEmail();
        
        /**
         * 步骤1：基础空值检查
         * 
         * 验证用户名和密码不能为空
         */
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return ResponseEntity.badRequest().body(new LoginResponse(null, null, null, null, "操作失败：用户名或密码不能为空。"));
        }

        /**
         * 步骤2：判断是邮箱还是用户名
         * 
         * 如果username包含@符号，则认为是邮箱登录
         */
        boolean isEmail = username.contains("@");
        
        /**
         * 步骤3：邮箱登录处理
         * 
         * 如果是邮箱登录，直接调用login方法
         * 邮箱登录不需要用户名格式验证
         */
        if (isEmail) {
            User user = userService.login(username, password);
            if (user != null) {
                /**
                 * 登录成功，生成JWT Token
                 */
                String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
                return ResponseEntity.ok(new LoginResponse(token, user.getId(), user.getUsername(), user.getRole(),
                        "登录成功！欢迎，" + user.getUsername() + "。您的ID是: " + user.getId()));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new LoginResponse(null, null, null, null, "操作失败：邮箱或密码错误，或账号已被禁用。"));
            }
        }

        /**
         * 步骤4：用户名登录/注册处理
         * 
         * 先验证用户名格式（仅针对新用户注册）
         * 如果是已有用户登录，允许登录（兼容旧数据）
         */
        UsernameValidator.ValidationResult validation = UsernameValidator.validate(username);
        if (!validation.isValid()) {
            /**
             * 格式验证失败，先尝试登录（兼容已有数据）
             * 
             * 如果已有用户使用旧格式的用户名，仍然允许登录
             */
            User existingUser = userService.login(username, password);
            if (existingUser != null) {
                /**
                 * 已有用户登录成功，允许登录（兼容旧数据）
                 */
                String token = jwtUtil.generateToken(existingUser.getId(), existingUser.getUsername(), existingUser.getRole());
                return ResponseEntity.ok(new LoginResponse(token, existingUser.getId(), existingUser.getUsername(), existingUser.getRole(),
                        "登录成功！欢迎，" + existingUser.getUsername() + "。您的ID是: " + existingUser.getId()));
            } else {
                /**
                 * 新用户注册，格式不对，拒绝注册
                 */
                return ResponseEntity.badRequest().body(new LoginResponse(null, null, null, null,
                    "注册失败：" + validation.getMessage() + "。用户名规则：长度3-20字符，只能包含字母、数字、下划线和中划线，必须以字母或数字开头。"));
            }
        }

        /**
         * 步骤5：格式验证通过，正常注册/登录流程
         * 
         * 先检查用户是否存在（用于判断是注册还是登录）
         */
        User existingUser = userService.login(username, password);
        
        /**
         * 步骤6：如果用户不存在，且提供了邮箱，检查邮箱是否已被其他用户使用
         */
        if (existingUser == null && email != null && !email.isEmpty()) {
            User emailUser = userService.checkEmailExists(email);
            if (emailUser != null && !emailUser.getUsername().equals(username)) {
                /**
                 * 邮箱已被其他用户使用
                 */
                return ResponseEntity.badRequest().body(new LoginResponse(null, null, null, null, "操作失败：邮箱已被使用。"));
            }
        }
        
        /**
         * 步骤7：正常注册/登录流程
         * 
         * 调用Service层的registerOrLogin方法
         * 该方法会自动判断是注册还是登录
         */
        User user = userService.registerOrLogin(username, password, email);
        if (user != null) {
            /**
             * 操作成功，生成JWT Token
             */
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
            return ResponseEntity.ok(new LoginResponse(token, user.getId(), user.getUsername(), user.getRole(),
                    "成功！欢迎，" + user.getUsername() + "。您的ID是: " + user.getId()));
        } else {
            /**
             * 操作失败，判断失败原因
             */
            // 先检查用户是否存在（不验证密码）
            User checkUser = userService.checkUsernameExists(username);
            if (checkUser == null) {
                // 用户不存在，检查是否是邮箱已被使用
                if (email != null && !email.isEmpty()) {
                    User emailCheck = userService.checkEmailExists(email);
                    if (emailCheck != null) {
                        return ResponseEntity.badRequest().body(new LoginResponse(null, null, null, null, "操作失败：邮箱已被使用。"));
                    }
                }
                return ResponseEntity.badRequest().body(new LoginResponse(null, null, null, null, "操作失败：注册失败，请检查输入信息。"));
            } else {
                // 用户存在但密码错误
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new LoginResponse(null, null, null, null, "操作失败：用户名或密码错误。"));
            }
        }
    }

    /**
     * 退出登录接口（将Token加入黑名单）
     * 
     * 请求路径: POST /api/user/logout
     * 
     * 说明：
     * - 将当前Token加入Redis黑名单
     * - Token在黑名单期间无法使用
     * - 客户端也应该删除本地存储的token
     * 
     * @param request HTTP请求对象，用于获取Token
     * @return ApiResponse对象，包含成功消息
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletRequest request) {
        // 从请求头中获取Token
        String token = getTokenFromRequest(request);
        
        if (token != null) {
            // 将Token加入黑名单
            jwtUtil.addTokenToBlacklist(token);
        }
        
        return ResponseEntity.ok(ApiResponse.success("退出登录成功！Token已失效，请客户端删除本地存储的token。"));
    }

    /**
     * 从请求头中获取Token（私有方法）
     * 
     * @param request HTTP请求对象
     * @return Token字符串，如果不存在则返回null
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        } else if (bearerToken != null) {
            return bearerToken;
        }
        return null;
    }

    /**
     * 获取当前用户信息
     * 
     * 请求路径: GET /api/user/current
     * 
     * 说明：
     * - 从JWT Token中获取userId（由JwtAuthenticationFilter设置）
     * - 查询用户信息并返回
     * 
     * @param request HTTP请求对象，包含userId属性（由过滤器设置）
     * @return ResponseEntity包含用户信息，如果未登录则返回null
     */
    @GetMapping("/current")
    /**
     * @GetMapping 注解：
     * - 处理GET请求
     * - 等价于@RequestMapping(method = RequestMethod.GET)
     */
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        /**
         * 从Request属性中获取userId
         * 
         * userId是由JwtAuthenticationFilter从Token中提取并设置的
         */
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();
        if (userId == null) {
            /**
             * 未登录，返回null
             */
            result.put("user", null);
            return ResponseEntity.ok(result);
        }
        /**
         * 已登录，查询用户信息
         */
        User user = userService.getUserById(userId);
        result.put("user", user);
        return ResponseEntity.ok(result);
    }

    /**
     * 修改个人信息
     * 
     * 请求路径: PUT /api/user/update
     * 
     * 请求体：User对象的JSON（只包含要更新的字段）
     * 
     * @param user 用户对象，包含要更新的字段
     * @param request HTTP请求对象，包含userId属性
     * @return ApiResponse对象，包含操作结果
     */
    @PutMapping("/update")
    /**
     * @PutMapping 注解：
     * - 处理PUT请求（用于更新资源）
     * - 等价于@RequestMapping(method = RequestMethod.PUT)
     */
    public ResponseEntity<ApiResponse> updateUser(@RequestBody User user, HttpServletRequest request) {
        /**
         * @RequestBody 注解说明：
         * - 从HTTP请求体中获取JSON数据
         * - 自动将JSON转换为User对象
         * - 适用于POST、PUT等请求
         */
        
        /**
         * 从Request属性中获取userId
         */
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.failureUnauthorized("更新失败：请先登录。");
        }

        /**
         * 步骤1：邮箱格式验证
         * 
         * 如果传了邮箱，先校验邮箱格式
         */
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            /**
             * 简单邮箱格式校验：包含@，且前后都至少有一个字符
             * 
             * 正则表达式说明：
             * - ^[A-Za-z0-9+_.-]+：邮箱用户名部分（字母、数字、+、_、.、-）
             * - @：@符号
             * - [A-Za-z0-9.-]+：域名部分（字母、数字、.、-）
             * - $：字符串结束
             */
            String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
            if (!user.getEmail().matches(emailRegex)) {
                return ApiResponse.failureBadRequest("更新失败：邮箱格式不正确。");
            }
        }

        /**
         * 步骤2：手机号格式验证
         * 
         * 如果传了手机号，先校验手机号格式
         */
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            /**
             * 中国手机号格式校验：11位数字，以1开头，第二位是3-9
             * 
             * 正则表达式说明：
             * - ^1：以1开头
             * - [3-9]：第二位是3-9
             * - \\d{9}：后面9位数字
             * - $：字符串结束
             */
            String phoneRegex = "^1[3-9]\\d{9}$";
            if (!user.getPhone().matches(phoneRegex)) {
                return ApiResponse.failureBadRequest("更新失败：手机号格式不正确。手机号应为11位数字，以1开头。");
            }
        }

        /**
         * 步骤3：设置用户ID并更新
         * 
         * 确保只能更新自己的信息
         */
        user.setId(userId);
        boolean success = userService.updateUser(user);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("个人信息更新成功！"));
        } else {
            return ApiResponse.failureBadRequest("更新失败！");
        }
    }

    /**
     * 获取收货地址列表
     * 
     * 请求路径: GET /api/user/addresses
     * 
     * @param request HTTP请求对象，包含userId属性
     * @return 地址列表，如果未登录则返回空列表
     */
    @GetMapping("/addresses")
    public List<Address> getUserAddresses(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            /**
             * 未登录，返回空列表
             * 
             * List.of()：创建不可变的空列表
             */
            return List.of();
        }
        return addressService.getUserAddresses(userId);
    }

    /**
     * 获取收货地址详情
     * 
     * 请求路径: GET /api/user/address/{id}
     * 
     * @param id 地址ID（路径变量）
     * @param request HTTP请求对象，包含userId属性
     * @return ApiResponse对象，包含地址信息
     */
    @GetMapping("/address/{id}")
    public ResponseEntity<ApiResponse> getAddressById(@PathVariable Long id, HttpServletRequest request) {
        /**
         * @PathVariable 注解说明：
         * - 从URL路径中获取变量值
         * - 例如：/api/user/address/123 -> id = 123
         */
        
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.failureUnauthorized("查询失败：请先登录。");
        }
        
        /**
         * 参数验证：地址ID必须有效
         */
        if (id == null || id <= 0) {
            return ApiResponse.failureBadRequest("查询失败：地址ID无效。");
        }
        
        /**
         * 查询地址（Service层会验证地址属于该用户）
         */
        Address address = addressService.getAddressById(id, userId);
        if (address != null) {
            /**
             * 双重验证：确保地址确实属于当前用户（防止SQL注入或其他问题）
             */
            if (!address.getUserId().equals(userId)) {
                return ApiResponse.failureBadRequest("查询失败：地址不属于您。");
            }
            return ResponseEntity.ok(ApiResponse.success("查询成功", address));
        } else {
            return ApiResponse.failureNotFound("查询失败：地址不存在或不属于您。");
        }
    }

    /**
     * 添加收货地址
     * 
     * 请求路径: POST /api/user/address
     * 
     * 请求体：Address对象的JSON
     * 
     * @param address 地址对象，包含地址信息
     * @param request HTTP请求对象，包含userId属性
     * @return ApiResponse对象，包含操作结果
     */
    @PostMapping("/address")
    public ResponseEntity<ApiResponse> addAddress(@RequestBody Address address, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.failureUnauthorized("添加失败：请先登录。");
        }

        /**
         * 步骤1：验证收货人姓名不能为空
         */
        if (address.getReceiverName() == null || address.getReceiverName().trim().isEmpty()) {
            return ApiResponse.failureBadRequest("添加失败！");
        }

        /**
         * 步骤2：验证收货人电话不能为空
         */
        if (address.getReceiverPhone() == null || address.getReceiverPhone().trim().isEmpty()) {
            return ApiResponse.failureBadRequest("添加失败！");
        }

        /**
         * 步骤3：验证收货人电话格式（中国手机号格式）
         */
        String phoneRegex = "^1[3-9]\\d{9}$";
        if (!address.getReceiverPhone().matches(phoneRegex)) {
            return ApiResponse.failureBadRequest("添加失败！");
        }

        /**
         * 步骤4：设置用户ID并添加地址
         */
        address.setUserId(userId);
        boolean success = addressService.addAddress(address);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("收货地址添加成功！"));
        } else {
            return ApiResponse.failureBadRequest("添加失败！");
        }
    }

    /**
     * 修改收货地址
     * 
     * 请求路径: PUT /api/user/address/{id}
     * 
     * 请求体：Address对象的JSON（只包含要更新的字段）
     * 
     * @param id 地址ID（路径变量）
     * @param address 地址对象，包含要更新的字段
     * @param request HTTP请求对象，包含userId属性
     * @return ApiResponse对象，包含操作结果
     */
    @PutMapping("/address/{id}")
    public ResponseEntity<ApiResponse> updateAddress(@PathVariable Long id, @RequestBody Address address, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.failureUnauthorized("更新失败：请先登录。");
        }

        /**
         * 步骤1：如果传了收货人姓名，验证不能为空
         */
        if (address.getReceiverName() != null && address.getReceiverName().trim().isEmpty()) {
            return ApiResponse.failureBadRequest("更新失败！");
        }

        /**
         * 步骤2：如果传了收货人电话，验证不能为空且格式正确
         */
        if (address.getReceiverPhone() != null) {
            if (address.getReceiverPhone().trim().isEmpty()) {
                return ApiResponse.failureBadRequest("更新失败！");
            }
            /**
             * 验证收货人电话格式（中国手机号格式）
             */
            String phoneRegex = "^1[3-9]\\d{9}$";
            if (!address.getReceiverPhone().matches(phoneRegex)) {
                return ApiResponse.failureBadRequest("更新失败！");
            }
        }

        /**
         * 步骤3：设置地址ID和用户ID并更新
         */
        address.setId(id);
        address.setUserId(userId);
        boolean success = addressService.updateAddress(address);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("收货地址更新成功！"));
        } else {
            return ApiResponse.failureBadRequest("更新失败！");
        }
    }

    /**
     * 删除收货地址
     * 
     * 请求路径: DELETE /api/user/address/{id}
     * 
     * @param id 地址ID（路径变量）
     * @param request HTTP请求对象，包含userId属性
     * @return ApiResponse对象，包含操作结果
     */
    @DeleteMapping("/address/{id}")
    /**
     * @DeleteMapping 注解：
     * - 处理DELETE请求（用于删除资源）
     * - 等价于@RequestMapping(method = RequestMethod.DELETE)
     */
    public ResponseEntity<ApiResponse> deleteAddress(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.failureUnauthorized("删除失败：请先登录。");
        }

        /**
         * 删除地址（Service层会验证地址属于该用户）
         */
        boolean success = addressService.deleteAddress(id, userId);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("收货地址删除成功！"));
        } else {
            return ApiResponse.failureBadRequest("删除失败！");
        }
    }
}
