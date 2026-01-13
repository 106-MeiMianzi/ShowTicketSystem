package com.mianzi.showticketsystem.filter;

import com.mianzi.showticketsystem.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * JWT认证过滤器
 * 拦截请求并验证JWT Token
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    // 不需要Token验证的路径
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/api/user/register-or-login",
            "/api/user/login",
            "/api/admin/login",
            "/api/show/home",
            "/api/show/search",
            "/api/show/conditions",
            "/api/payment/notify"  // 支付回调接口由支付宝服务器调用，不需要JWT验证
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        
        // 检查是否是需要排除的路径
        if (isExcludePath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 从请求头中获取Token
        String token = getTokenFromRequest(request);
        
        if (token == null || !jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"未授权：请先登录\"}");
            return;
        }

        // 验证Token并将用户信息存储到Request属性中
        Long userId = jwtUtil.getUserIdFromToken(token);
        Integer role = jwtUtil.getRoleFromToken(token);
        
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"未授权：Token无效\"}");
            return;
        }

        request.setAttribute("userId", userId);
        request.setAttribute("role", role);

        // 对于管理员接口，检查是否为管理员（排除登录接口）
        if (requestPath.startsWith("/api/admin/") && !requestPath.equals("/api/admin/login")) {
            if (role == null || role != 2) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"禁止访问：需要管理员权限\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中获取Token
     * 支持两种格式：Authorization: Bearer <token> 或 Authorization: <token>
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        } else if (StringUtils.hasText(bearerToken)) {
            return bearerToken;
        }
        return null;
    }

    /**
     * 判断是否为排除路径
     */
    private boolean isExcludePath(String path) {
        for (String excludePath : EXCLUDE_PATHS) {
            if (path.equals(excludePath) || path.startsWith(excludePath + "/")) {
                return true;
            }
        }
        return false;
    }
}

