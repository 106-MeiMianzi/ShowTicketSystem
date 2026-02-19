package com.mianzi.showticketsystem.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流注解
 * 
 * 作用：
 * - 标记需要限流的接口
 * - 防止接口被恶意刷
 * 
 * 使用示例：
 * @RateLimit(maxRequests = 5, timeWindow = 60) // 每分钟最多5次请求
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    /**
     * 时间窗口内的最大请求数
     * 
     * @return 最大请求数，默认10次
     */
    int maxRequests() default 10;

    /**
     * 时间窗口（秒）
     * 
     * @return 时间窗口，默认60秒
     */
    int timeWindow() default 60;

    /**
     * 限流键的前缀
     * 
     * @return 键前缀，默认"rate"
     */
    String keyPrefix() default "rate";
}
