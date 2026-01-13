package com.mianzi.showticketsystem.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 统一API响应DTO
 * 用于返回操作结果（成功/失败消息）和数据
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {
    private String message;
    private Boolean success;
    private Object data; // 可选：用于返回数据

    public ApiResponse(String message, Boolean success, Object data) {
        this.message = message;
        this.success = success;
        this.data = data;
    }

    /**
     * 创建成功响应（带数据）
     */
    public static ApiResponse success(String message, Object data) {
        return new ApiResponse(message, true, data);
    }

    /**
     * 创建成功响应（不带数据）
     */
    public static ApiResponse success(String message) {
        return new ApiResponse(message, true, null);
    }

    /**
     * 创建失败响应（带数据）
     */
    public static ApiResponse failure(String message, Object data) {
        return new ApiResponse(message, false, data);
    }

    /**
     * 创建失败响应（不带数据）
     * 返回空对象 {} 以满足Apifox接口定义要求
     */
    public static ApiResponse failure(String message) {
        return new ApiResponse(message, false, new java.util.HashMap<>());
    }
}

