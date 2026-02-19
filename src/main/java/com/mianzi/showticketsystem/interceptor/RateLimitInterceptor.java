package com.mianzi.showticketsystem.interceptor;

import com.mianzi.showticketsystem.annotation.RateLimit;
import com.mianzi.showticketsystem.util.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * 接口限流拦截器
 * 
 * 作用：
 * - 拦截HTTP请求，检查是否超过限流阈值
 * - 使用Redis计数器实现限流
 * 
 * 说明：
 * - 基于用户ID或IP地址进行限流
 * - 使用滑动窗口算法
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        // 只处理方法级别的拦截
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);

        // 如果没有@RateLimit注解，直接放行
        if (rateLimit == null) {
            return true;
        }

        // 获取限流键（基于用户ID或IP）
        String key = getRateLimitKey(request, rateLimit.keyPrefix());

        // 检查是否超过限流阈值
        if (!checkRateLimit(key, rateLimit.maxRequests(), rateLimit.timeWindow())) {
            // 超过限流阈值，返回429 Too Many Requests
            // 注意：Jakarta Servlet API 中没有 SC_TOO_MANY_REQUESTS 常量，直接使用429
            try {
                response.setStatus(429); // HTTP 429 Too Many Requests
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"请求过于频繁，请稍后再试\"}");
            } catch (IOException e) {
                // 如果写入响应失败，记录日志但不影响流程
                // 在实际项目中应该使用日志框架记录
            }
            return false;
        }

        return true;
    }

    /**
     * 获取限流键
     * 
     * @param request HTTP请求
     * @param prefix 键前缀
     * @return 限流键
     */
    private String getRateLimitKey(HttpServletRequest request, String prefix) {
        // 优先使用用户ID（如果已登录）
        Long userId = (Long) request.getAttribute("userId");
        if (userId != null) {
            return prefix + ":user:" + userId;
        }

        // 未登录则使用IP地址
        String ip = getClientIp(request);
        return prefix + ":ip:" + ip;
    }

    /**
     * 获取客户端IP地址
     * 
     * @param request HTTP请求
     * @return IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个IP的情况（取第一个）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 检查是否超过限流阈值
     * 
     * @param key 限流键
     * @param maxRequests 最大请求数
     * @param timeWindow 时间窗口（秒）
     * @return true表示未超过限流，false表示超过限流
     */
    private boolean checkRateLimit(String key, int maxRequests, int timeWindow) {
        Long count = redisUtil.increment(key, 1);
        
        if (count == 1) {
            // 第一次请求，设置过期时间
            redisUtil.expire(key, timeWindow);
        }

        return count <= maxRequests;
    }
}
