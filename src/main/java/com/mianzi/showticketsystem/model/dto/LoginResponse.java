package com.mianzi.showticketsystem.model.dto;

import lombok.Data;

/**
 * 登录响应DTO（Data Transfer Object）
 * 
 * 作用：
 * - 封装用户登录成功后的响应数据
 * - 包含 JWT Token 和用户基本信息
 * - 用于返回给前端，前端保存 Token 用于后续请求认证
 * 
 * 响应格式示例：
 * {
 *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "userId": 1,
 *   "username": "张三",
 *   "role": 1,
 *   "message": "登录成功！欢迎，张三。您的ID是: 1"
 * }
 */
@Data
/**
 * @Data 注解：自动生成 getter、setter、toString、equals、hashCode 等方法
 */
public class LoginResponse {
    
    /**
     * JWT Token
     * 
     * 说明：
     * - 用户身份认证令牌
     * - 前端需要保存这个 Token（通常存储在 localStorage 或 sessionStorage）
     * - 后续请求需要在 HTTP Header 中携带：Authorization: Bearer {token}
     * - Token 有过期时间，过期后需要重新登录
     */
    private String token;
    
    /**
     * 用户ID
     * 
     * 说明：
     * - 用户的唯一标识
     * - 前端可以用来标识当前登录用户
     */
    private Long userId;
    
    /**
     * 用户名
     * 
     * 说明：
     * - 用户的登录名
     * - 用于前端显示当前登录用户
     */
    private String username;
    
    /**
     * 用户角色
     * 
     * 说明：
     * - 1：普通用户
     * - 2：管理员
     * 
     * 用途：
     * - 前端可以根据角色显示不同的菜单和功能
     * - 用于权限控制
     */
    private Integer role;
    
    /**
     * 响应消息
     * 
     * 说明：
     * - 登录结果的提示信息
     * - 成功时：如"登录成功！欢迎，张三。您的ID是: 1"
     * - 失败时：如"登录失败：账号或密码错误，或账号已被禁用"
     */
    private String message;

    /**
     * 构造函数
     * 
     * @param token JWT Token（登录成功时传入，失败时为 null）
     * @param userId 用户ID（登录成功时传入，失败时为 null）
     * @param username 用户名（登录成功时传入，失败时为 null）
     * @param role 用户角色（登录成功时传入，失败时为 null）
     * @param message 响应消息
     */
    public LoginResponse(String token, Long userId, String username, Integer role, String message) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.message = message;
    }
}

