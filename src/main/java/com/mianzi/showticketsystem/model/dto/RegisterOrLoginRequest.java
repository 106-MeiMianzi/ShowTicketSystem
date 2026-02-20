package com.mianzi.showticketsystem.model.dto;

import lombok.Data;

/**
 * 注册/登录请求DTO（Data Transfer Object）
 * 
 * 作用：
 * - 封装用户注册/登录的请求数据
 * - 接收前端发送的JSON格式请求体
 * - 用于统一处理注册和登录接口的请求参数
 * 
 * 请求格式示例：
 * {
 *   "username": "testuser",
 *   "password": "test123",
 *   "email": "test@example.com"  // 可选
 * }
 * 
 * 说明：
 * - username: 用户名或邮箱（必填），如果包含@符号则识别为邮箱登录
 * - password: 密码（必填）
 * - email: 邮箱（可选），注册时建议提供
 */
@Data
/**
 * @Data 注解：自动生成 getter、setter、toString、equals、hashCode 等方法
 */
public class RegisterOrLoginRequest {
    
    /**
     * 用户名或邮箱
     * 
     * 说明：
     * - 必填字段
     * - 如果包含@符号，系统会自动识别为邮箱登录
     * - 如果不包含@符号，则作为用户名处理
     * 
     * 示例：
     * - "testuser" -> 用户名登录/注册
     * - "test@example.com" -> 邮箱登录
     */
    private String username;
    
    /**
     * 密码
     * 
     * 说明：
     * - 必填字段
     * - 用户登录或注册时使用的密码
     */
    private String password;
    
    /**
     * 邮箱
     * 
     * 说明：
     * - 可选字段
     * - 注册新用户时建议提供，用于账户找回等功能
     * - 如果username是邮箱格式，此字段可以为空
     */
    private String email;
}
