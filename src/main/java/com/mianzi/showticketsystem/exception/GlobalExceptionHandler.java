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
 * 
 * 作用：
 * - 统一处理应用中的异常，返回友好的错误信息
 * - 避免异常直接暴露给前端
 * - 提供统一的错误响应格式
 * 
 * 工作原理：
 * - 使用@ControllerAdvice注解，拦截所有Controller抛出的异常
 * - 使用@ExceptionHandler注解，处理特定类型的异常
 * - 返回统一的ApiResponse格式，包含错误消息
 */
@ControllerAdvice
/**
 * @ControllerAdvice 注解说明：
 * 
 * Spring MVC提供的全局异常处理注解
 * 
 * 作用：
 * - 标识这是一个全局异常处理器
 * - 可以拦截所有Controller抛出的异常
 * - 提供统一的异常处理逻辑
 * 
 * 与@ExceptionHandler配合使用：
 * - @ExceptionHandler指定要处理的异常类型
 * - 当Controller抛出该类型异常时，会自动调用对应的处理方法
 */
public class GlobalExceptionHandler {

    /**
     * 处理JSON反序列化异常
     * 
     * 触发场景：
     * - 前端传递的JSON数据格式错误
     * - 类型转换失败（如将字符串"abc"转换为BigDecimal）
     * - JSON格式不正确（如缺少引号、括号不匹配等）
     * 
     * @param e HttpMessageNotReadableException异常对象
     * @return ResponseEntity包含错误响应
     * 
     * 示例：
     * - 前端传递：{"price": "abc"} -> 无法转换为BigDecimal -> 触发此异常
     * - 前端传递：{"price": 100.00} -> 正常转换
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        /**
         * 默认错误消息
         */
        String message = "请求参数格式错误";
        
        /**
         * 获取异常的详细消息
         * 
         * 异常消息通常包含字段名和错误原因
         * 例如："JSON parse error: Cannot deserialize value of type `java.math.BigDecimal` from String \"abc\""
         */
        String exceptionMessage = e.getMessage();
        if (exceptionMessage != null) {
            /**
             * 转换为小写，便于匹配关键词
             */
            exceptionMessage = exceptionMessage.toLowerCase();
            
            /**
             * 根据异常消息中的关键词，提供更具体的错误提示
             */
            
            // 检查是否是价格字段错误
            if (exceptionMessage.contains("price") || exceptionMessage.contains("bigdecimal")) {
                message = "添加失败！价格格式不正确，应为数字类型（如：100.00）。";
            } 
            // 检查是否是总票数字段错误
            else if (exceptionMessage.contains("totaltickets") || exceptionMessage.contains("integer")) {
                message = "添加失败！总票数格式不正确，应为整数类型。";
            } 
            // 检查是否是日期时间字段错误
            else if (exceptionMessage.contains("starttime") || exceptionMessage.contains("endtime") || 
                       exceptionMessage.contains("localdatetime") || exceptionMessage.contains("date")) {
                message = "添加失败！日期时间格式不正确，应为 ISO 8601 格式（如：2024-12-31T20:00:00）。";
            } 
            // 检查是否是其他数字字段错误
            else if (exceptionMessage.contains("bigdecimal") || exceptionMessage.contains("number")) {
                message = "添加失败！数字格式不正确，请检查价格、总票数等数字字段。";
            }
        }
        
        /**
         * 检查异常原因链
         * 
         * 有时异常消息在cause中，需要进一步检查
         */
        Throwable cause = e.getCause();
        if (cause != null) {
            String causeMessage = cause.getMessage();
            if (causeMessage != null) {
                causeMessage = causeMessage.toLowerCase();
                /**
                 * 如果cause中包含price相关错误，优先使用price的错误消息
                 */
                if (causeMessage.contains("price") || (causeMessage.contains("bigdecimal") && causeMessage.contains("price"))) {
                    message = "添加失败！价格格式不正确，应为数字类型（如：100.00）。";
                }
            }
        }
        
        /**
         * 返回400 Bad Request错误响应
         * 
         * HttpStatus.BAD_REQUEST：400状态码，表示客户端请求错误
         * ApiResponse.failure(message)：创建失败响应，包含错误消息
         */
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(message));
    }

    /**
     * 处理参数类型不匹配异常
     * 
     * 触发场景：
     * - URL参数类型错误（如将字符串"abc"转换为Long）
     * - 路径变量类型错误（如@PathVariable Long id，但URL中是"abc"）
     * 
     * @param e MethodArgumentTypeMismatchException异常对象
     * @return ResponseEntity包含错误响应
     * 
     * 示例：
     * - URL: /api/user/abc -> @PathVariable Long id -> 无法转换 -> 触发此异常
     * - URL: /api/user/123 -> @PathVariable Long id -> 正常转换
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        /**
         * 默认错误消息
         */
        String message = "请求参数类型错误";
        
        /**
         * 获取参数名和期望类型
         * 
         * getName()：获取参数名（如"id"、"price"）
         * getRequiredType()：获取期望的类型（如Long.class、BigDecimal.class）
         */
        String paramName = e.getName();
        Class<?> requiredType = e.getRequiredType();
        
        /**
         * 根据参数类型，提供更具体的错误提示
         */
        if (paramName != null && requiredType != null) {
            /**
             * 如果是BigDecimal类型（价格等）
             */
            if (requiredType == java.math.BigDecimal.class) {
                message = "添加失败！" + paramName + " 格式不正确，应为数字类型（如：100.00）。";
            } 
            /**
             * 如果是Integer或Long类型（ID、数量等）
             */
            else if (requiredType == Integer.class || requiredType == Long.class) {
                message = "添加失败！" + paramName + " 格式不正确，应为整数类型。";
            } 
            /**
             * 其他类型
             */
            else {
                message = "添加失败！" + paramName + " 格式不正确，期望类型：" + requiredType.getSimpleName() + "。";
            }
        }
        
        /**
         * 返回400 Bad Request错误响应
         */
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(message));
    }
}
