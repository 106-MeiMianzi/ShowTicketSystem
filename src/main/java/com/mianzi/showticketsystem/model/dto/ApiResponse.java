package com.mianzi.showticketsystem.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 统一API响应DTO（Data Transfer Object）
 * 
 * 作用：
 * - 统一所有API接口的响应格式
 * - 包含操作结果（成功/失败）和消息
 * - 可选地包含返回的数据
 * 
 * 响应格式示例：
 * {
 *   "message": "操作成功",
 *   "success": true,
 *   "data": { ... }  // 可选
 * }
 * 
 * 优势：
 * - 前端可以统一处理响应格式
 * - 便于错误处理和消息提示
 */
@Data
/**
 * @Data 注解：自动生成 getter、setter、toString、equals、hashCode 等方法
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
/**
 * @JsonInclude(JsonInclude.Include.NON_NULL) 注解说明：
 * 
 * Jackson 序列化配置
 * 
 * 作用：
 * - 当对象转换为 JSON 时，如果字段值为 null，则不包含在 JSON 中
 * - 使 JSON 响应更简洁，避免返回 null 值
 * 
 * 示例：
 * - data 为 null 时，JSON 中不包含 "data" 字段
 * - data 有值时，JSON 中包含 "data" 字段
 */
public class ApiResponse {
    
    /**
     * 响应消息
     * 
     * 说明：
     * - 操作结果的描述信息
     * - 成功时：如"操作成功"、"用户信息更新成功"
     * - 失败时：如"操作失败"、"用户名或密码错误"
     */
    private String message;
    
    /**
     * 操作是否成功
     * 
     * 说明：
     * - true：操作成功
     * - false：操作失败
     * 
     * 类型：Boolean（包装类型），可以为 null
     */
    private Boolean success;
    
    /**
     * 响应数据（可选）
     * 
     * 说明：
     * - 操作成功时返回的数据
     * - 类型为 Object，可以返回任何类型的数据
     * - 如果不需要返回数据，可以为 null（会被 @JsonInclude 排除）
     * 
     * 使用示例：
     * - 查询用户信息：data = User 对象
     * - 查询列表：data = List<User>
     * - 简单操作：data = null（只返回成功消息）
     */
    private Object data;

    /**
     * 构造函数
     * 
     * @param message 响应消息
     * @param success 是否成功
     * @param data 响应数据（可以为 null）
     */
    public ApiResponse(String message, Boolean success, Object data) {
        this.message = message;
        this.success = success;
        this.data = data;
    }

    /**
     * 创建成功响应（带数据）
     * 
     * 静态工厂方法，提供便捷的创建方式
     * 
     * @param message 成功消息
     * @param data 返回的数据
     * @return ApiResponse 对象，success = true
     * 
     * 使用示例：
     * User user = userService.getUserById(1L);
     * return ApiResponse.success("查询成功", user);
     */
    public static ApiResponse success(String message, Object data) {
        return new ApiResponse(message, true, data);
    }

    /**
     * 创建成功响应（不带数据）
     * 
     * @param message 成功消息
     * @return ApiResponse 对象，success = true，data = null
     * 
     * 使用示例：
     * return ApiResponse.success("操作成功");
     */
    public static ApiResponse success(String message) {
        return new ApiResponse(message, true, null);
    }

    /**
     * 创建失败响应（带数据）
     * 
     * @param message 失败消息
     * @param data 返回的数据（错误详情等）
     * @return ApiResponse 对象，success = false
     * 
     * 使用示例：
     * return ApiResponse.failure("操作失败", errorDetails);
     */
    public static ApiResponse failure(String message, Object data) {
        return new ApiResponse(message, false, data);
    }

    /**
     * 创建失败响应（不带数据）
     * 
     * @param message 失败消息
     * @return ApiResponse 对象，success = false，data = 空 HashMap
     * 
     * 说明：
     * - 返回空对象 {} 而不是 null，以满足 Apifox 接口定义要求
     * - 确保 JSON 响应格式一致
     * 
     * 使用示例：
     * return ApiResponse.failure("用户名或密码错误");
     */
    public static ApiResponse failure(String message) {
        /**
         * 返回空的 HashMap 而不是 null
         * 
         * 原因：
         * - 某些 API 文档工具（如 Apifox）要求响应格式一致
         * - 即使没有数据，也返回一个空对象 {}
         * - 避免前端处理 null 值的问题
         */
        return new ApiResponse(message, false, new java.util.HashMap<>());
    }
}

