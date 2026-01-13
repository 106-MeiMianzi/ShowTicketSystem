package com.mianzi.showticketsystem.exception;

import com.mianzi.showticketsystem.model.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 全局异常处理器
 * 用于统一处理应用中的异常，返回友好的错误信息
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 JSON 反序列化异常（如类型转换错误）
     * 例如：将字符串 "abc" 转换为 BigDecimal 时会抛出此异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        String message = "请求参数格式错误";
        
        // 获取完整的异常消息
        String exceptionMessage = e.getMessage();
        if (exceptionMessage != null) {
            exceptionMessage = exceptionMessage.toLowerCase();
            // 检查是否是价格字段错误
            if (exceptionMessage.contains("price") || exceptionMessage.contains("bigdecimal")) {
                message = "添加失败！价格格式不正确，应为数字类型（如：100.00）。";
            } else if (exceptionMessage.contains("totaltickets") || exceptionMessage.contains("integer")) {
                message = "添加失败！总票数格式不正确，应为整数类型。";
            } else if (exceptionMessage.contains("starttime") || exceptionMessage.contains("endtime") || 
                       exceptionMessage.contains("localdatetime") || exceptionMessage.contains("date")) {
                message = "添加失败！日期时间格式不正确，应为 ISO 8601 格式（如：2024-12-31T20:00:00）。";
            } else if (exceptionMessage.contains("bigdecimal") || exceptionMessage.contains("number")) {
                message = "添加失败！数字格式不正确，请检查价格、总票数等数字字段。";
            }
        }
        
        // 检查异常原因链
        Throwable cause = e.getCause();
        if (cause != null) {
            String causeMessage = cause.getMessage();
            if (causeMessage != null) {
                causeMessage = causeMessage.toLowerCase();
                if (causeMessage.contains("price") || (causeMessage.contains("bigdecimal") && causeMessage.contains("price"))) {
                    message = "添加失败！价格格式不正确，应为数字类型（如：100.00）。";
                }
            }
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(message));
    }

    /**
     * 处理参数类型不匹配异常
     * 例如：URL 参数类型错误
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String message = "请求参数类型错误";
        String paramName = e.getName();
        Class<?> requiredType = e.getRequiredType();
        
        if (paramName != null && requiredType != null) {
            if (requiredType == java.math.BigDecimal.class) {
                message = "添加失败！" + paramName + " 格式不正确，应为数字类型（如：100.00）。";
            } else if (requiredType == Integer.class || requiredType == Long.class) {
                message = "添加失败！" + paramName + " 格式不正确，应为整数类型。";
            } else {
                message = "添加失败！" + paramName + " 格式不正确，期望类型：" + requiredType.getSimpleName() + "。";
            }
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(message));
    }
}
