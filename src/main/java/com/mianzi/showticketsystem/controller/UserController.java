package com.mianzi.showticketsystem.controller;

import com.mianzi.showticketsystem.model.dto.ApiResponse;
import com.mianzi.showticketsystem.model.dto.LoginResponse;
import com.mianzi.showticketsystem.model.entity.Address;
import com.mianzi.showticketsystem.model.entity.User;
import com.mianzi.showticketsystem.service.AddressService;
import com.mianzi.showticketsystem.service.UserService;
import com.mianzi.showticketsystem.util.JwtUtil;
import com.mianzi.showticketsystem.util.UsernameValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户接口控制器
 * 接收来自前端的HTTP请求，并调用Service层（UserService）的业务逻辑
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户注册/登录接口（合并接口）
     * 如果用户不存在则注册，存在则登录
     * 支持用户名和邮箱登录
     * 完整请求路径: POST /api/user/register-or-login
     */
    @PostMapping("/register-or-login")
    public LoginResponse registerOrLogin(@RequestParam String username,
                                         @RequestParam String password,
                                         @RequestParam(required = false) String email) {
        // 基础空值检查
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return new LoginResponse(null, null, null, null, "操作失败：用户名或密码不能为空。");
        }

        // 判断是邮箱还是用户名（包含@符号则为邮箱）
        boolean isEmail = username.contains("@");
        
        // 如果是邮箱登录，直接调用login方法（邮箱登录不需要用户名格式验证）
        if (isEmail) {
            User user = userService.login(username, password);
            if (user != null) {
                String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
                return new LoginResponse(token, user.getId(), user.getUsername(), user.getRole(),
                        "登录成功！欢迎，" + user.getUsername() + "。您的ID是: " + user.getId());
            } else {
                return new LoginResponse(null, null, null, null, "操作失败：邮箱或密码错误，或账号已被禁用。");
            }
        }

        // 用户名登录/注册：验证用户名格式（仅针对新用户注册）
        // 如果是已有用户登录，允许登录（兼容旧数据）
        UsernameValidator.ValidationResult validation = UsernameValidator.validate(username);
        if (!validation.isValid()) {
            // 格式验证失败，先尝试登录（兼容已有数据）
            User existingUser = userService.login(username, password);
            if (existingUser != null) {
                // 已有用户登录成功，允许登录（兼容旧数据）
                String token = jwtUtil.generateToken(existingUser.getId(), existingUser.getUsername(), existingUser.getRole());
                return new LoginResponse(token, existingUser.getId(), existingUser.getUsername(), existingUser.getRole(),
                        "登录成功！欢迎，" + existingUser.getUsername() + "。您的ID是: " + existingUser.getId());
            } else {
                // 新用户注册，格式不对，拒绝注册
                return new LoginResponse(null, null, null, null, 
                    "注册失败：" + validation.getMessage() + "。用户名规则：长度3-20字符，只能包含字母、数字、下划线和中划线，必须以字母或数字开头。");
            }
        }

        // 格式验证通过，正常注册/登录流程
        // 先检查用户是否存在（用于判断是注册还是登录）
        User existingUser = userService.login(username, password);
        
        // 如果用户不存在，且提供了邮箱，检查邮箱是否已被其他用户使用
        if (existingUser == null && email != null && !email.isEmpty()) {
            User emailUser = userService.checkEmailExists(email);
            if (emailUser != null && !emailUser.getUsername().equals(username)) {
                // 邮箱已被其他用户使用
                return new LoginResponse(null, null, null, null, "操作失败：邮箱已被使用。");
            }
        }
        
        // 正常注册/登录流程
        User user = userService.registerOrLogin(username, password, email);
        if (user != null) {
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
            return new LoginResponse(token, user.getId(), user.getUsername(), user.getRole(),
                    "成功！欢迎，" + user.getUsername() + "。您的ID是: " + user.getId());
        } else {
            // 判断失败原因
            // 先检查用户是否存在（不验证密码）
            User checkUser = userService.checkUsernameExists(username);
            if (checkUser == null) {
                // 用户不存在，检查是否是邮箱已被使用
                if (email != null && !email.isEmpty()) {
                    User emailCheck = userService.checkEmailExists(email);
                    if (emailCheck != null) {
                        return new LoginResponse(null, null, null, null, "操作失败：邮箱已被使用。");
                    }
                }
                return new LoginResponse(null, null, null, null, "操作失败：注册失败，请检查输入信息。");
            } else {
                // 用户存在但密码错误
                return new LoginResponse(null, null, null, null, "操作失败：用户名或密码错误。");
            }
        }
    }

    /**
     * 退出登录接口
     * 完整请求路径: POST /api/user/logout
     * 注意：JWT是无状态的，客户端删除token即可，服务端不需要特殊处理
     */
    @PostMapping("/logout")
    public ApiResponse logout() {
        return ApiResponse.success("退出登录成功！请客户端删除本地存储的token。");
    }

    /**
     * 获取当前用户信息
     * 完整请求路径: GET /api/user/current
     */
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();
        if (userId == null) {
            result.put("user", null);
            return ResponseEntity.ok(result);
        }
        User user = userService.getUserById(userId);
        result.put("user", user);
        return ResponseEntity.ok(result);
    }

    /**
     * 修改个人信息
     * 完整请求路径: PUT /api/user/update
     */
    @PutMapping("/update")
    public ApiResponse updateUser(@RequestBody User user, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.failure("更新失败：请先登录。");
        }

        // 如果传了邮箱，先校验邮箱格式
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            // 简单邮箱格式校验：包含 @，且前后都至少有一个字符
            String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
            if (!user.getEmail().matches(emailRegex)) {
                return ApiResponse.failure("更新失败：邮箱格式不正确。");
            }
        }

        // 如果传了手机号，先校验手机号格式
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            // 中国手机号格式校验：11位数字，以1开头，第二位是3-9
            String phoneRegex = "^1[3-9]\\d{9}$";
            if (!user.getPhone().matches(phoneRegex)) {
                return ApiResponse.failure("更新失败：手机号格式不正确。手机号应为11位数字，以1开头。");
            }
        }

        user.setId(userId);
        boolean success = userService.updateUser(user);
        if (success) {
            return ApiResponse.success("个人信息更新成功！");
        } else {
            return ApiResponse.failure("更新失败！");
        }
    }

    /**
     * 获取收货地址列表
     * 完整请求路径: GET /api/user/addresses
     */
    @GetMapping("/addresses")
    public List<Address> getUserAddresses(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return List.of();
        }
        return addressService.getUserAddresses(userId);
    }

    /**
     * 获取收货地址详情
     * 完整请求路径: GET /api/user/address/{id}
     */
    @GetMapping("/address/{id}")
    public ApiResponse getAddressById(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.failure("查询失败：请先登录。");
        }
        
        // 参数验证
        if (id == null || id <= 0) {
            return ApiResponse.failure("查询失败：地址ID无效。");
        }
        
        Address address = addressService.getAddressById(id, userId);
        if (address != null) {
            // 双重验证：确保地址确实属于当前用户（防止SQL注入或其他问题）
            if (!address.getUserId().equals(userId)) {
                return ApiResponse.failure("查询失败：地址不属于您。");
            }
            return ApiResponse.success("查询成功", address);
        } else {
            return ApiResponse.failure("查询失败：地址不存在或不属于您。");
        }
    }

    /**
     * 添加收货地址
     * 完整请求路径: POST /api/user/address
     */
    @PostMapping("/address")
    public ApiResponse addAddress(@RequestBody Address address, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.failure("添加失败：请先登录。");
        }

        // 验证收货人姓名不能为空
        if (address.getReceiverName() == null || address.getReceiverName().trim().isEmpty()) {
            return ApiResponse.failure("添加失败！");
        }

        // 验证收货人电话不能为空
        if (address.getReceiverPhone() == null || address.getReceiverPhone().trim().isEmpty()) {
            return ApiResponse.failure("添加失败！");
        }

        // 验证收货人电话格式（中国手机号格式）
        String phoneRegex = "^1[3-9]\\d{9}$";
        if (!address.getReceiverPhone().matches(phoneRegex)) {
            return ApiResponse.failure("添加失败！");
        }

        address.setUserId(userId);
        boolean success = addressService.addAddress(address);
        if (success) {
            return ApiResponse.success("收货地址添加成功！");
        } else {
            return ApiResponse.failure("添加失败！");
        }
    }

    /**
     * 修改收货地址
     * 完整请求路径: PUT /api/user/address/{id}
     */
    @PutMapping("/address/{id}")
    public ApiResponse updateAddress(@PathVariable Long id, @RequestBody Address address, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.failure("更新失败：请先登录。");
        }

        // 如果传了收货人姓名，验证不能为空
        if (address.getReceiverName() != null && address.getReceiverName().trim().isEmpty()) {
            return ApiResponse.failure("更新失败！");
        }

        // 如果传了收货人电话，验证不能为空且格式正确
        if (address.getReceiverPhone() != null) {
            if (address.getReceiverPhone().trim().isEmpty()) {
                return ApiResponse.failure("更新失败！");
            }
            // 验证收货人电话格式（中国手机号格式）
            String phoneRegex = "^1[3-9]\\d{9}$";
            if (!address.getReceiverPhone().matches(phoneRegex)) {
                return ApiResponse.failure("更新失败！");
            }
        }

        address.setId(id);
        address.setUserId(userId);
        boolean success = addressService.updateAddress(address);
        if (success) {
            return ApiResponse.success("收货地址更新成功！");
        } else {
            return ApiResponse.failure("更新失败！");
        }
    }

    /**
     * 删除收货地址
     * 完整请求路径: DELETE /api/user/address/{id}
     */
    @DeleteMapping("/address/{id}")
    public ApiResponse deleteAddress(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.failure("删除失败：请先登录。");
        }

        boolean success = addressService.deleteAddress(id, userId);
        if (success) {
            return ApiResponse.success("收货地址删除成功！");
        } else {
            return ApiResponse.failure("删除失败！");
        }
    }
}
