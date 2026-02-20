package com.mianzi.showticketsystem.filter;

import com.mianzi.showticketsystem.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * JWT认证过滤器
 * 
 * 作用：
 * - 拦截HTTP请求并验证JWT Token
 * - 从请求头中提取Token并验证
 * - 将用户信息存储到Request属性中，供Controller使用
 * - 实现请求级别的身份认证和权限控制
 * 
 * 工作原理：
 * 1. 拦截所有/api/*路径的请求
 * 2. 检查请求路径是否在排除列表中（如登录接口）
 * 3. 从请求头中提取Token
 * 4. 验证Token是否有效
 * 5. 从Token中提取用户信息并存储到Request属性中
 * 6. 对于管理员接口，检查用户角色
 */
@Component
/**
 * @Component 注解说明：
 * 
 * 标识这是一个Spring组件（Bean）
 * 
 * 作用：
 * - Spring会自动扫描并创建这个类的实例
 * - 可以在WebConfig中通过@Autowired注入使用
 * - 单例模式，整个应用只有一个实例
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    /**
     * OncePerRequestFilter说明：
     * 
     * Spring提供的过滤器基类
     * 
     * 特点：
     * - 确保每个请求只执行一次过滤逻辑
     * - 避免在forward或include时重复执行
     * - 需要实现doFilterInternal方法
     */

    /**
     * 注入JWT工具类
     * 
     * @Autowired 注解：
     * - 自动注入JwtUtil的实例
     * - 用于生成、验证、解析Token
     */
    @Autowired
    private JwtUtil jwtUtil;

    /** 为 true 时 401 响应中返回具体失败原因（仅调试用，生产请关闭） */
    @Value("${jwt.debug:false}")
    private boolean jwtDebug;

    /**
     * 不需要Token验证的路径列表
     * 
     * 说明：
     * - 这些路径是公开接口，不需要登录即可访问
     * - 登录接口本身不需要Token验证
     * - 首页、搜索等公开功能也不需要Token验证
     */
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/api/user/register-or-login",  // 用户注册/登录接口
            "/api/user/login",              // 用户登录接口（如果单独存在）
            "/api/admin/login",             // 管理员登录接口
            "/api/show/home",               // 演出首页（公开）
            "/api/show/search",             // 演出搜索（公开）
            "/api/show/conditions",        // 演出条件查询（公开）
            "/api/payment/notify"           // 支付回调接口（由支付宝服务器调用，不需要JWT验证）
    );

    /**
     * 过滤器的核心方法
     * 
     * 执行流程：
     * 1. 检查请求路径是否在排除列表中
     * 2. 从请求头中提取Token
     * 3. 验证Token是否有效
     * 4. 从Token中提取用户信息并存储到Request属性中
     * 5. 对于管理员接口，检查用户角色
     * 6. 继续执行后续的过滤器或Controller
     * 
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param filterChain 过滤器链，用于继续执行后续过滤器
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        /**
         * 获取请求路径
         * 
         * getRequestURI()：获取请求的URI（不包含查询参数）
         * 例如：/api/user/current
         */
        String requestPath = request.getRequestURI();
        
        /**
         * 步骤1：检查是否是需要排除的路径
         * 
         * 如果是公开接口（如登录、首页），直接放行，不进行Token验证
         */
        if (isExcludePath(requestPath)) {
            /**
             * doFilter()：继续执行后续的过滤器或Controller
             * 
             * 如果不在排除列表中，会继续执行Token验证逻辑
             */
            filterChain.doFilter(request, response);
            return;
        }

        /**
         * 步骤2：从请求头中获取Token
         * 
         * 支持两种格式：
         * - Authorization: Bearer <token>
         * - Authorization: <token>
         */
        String token = getTokenFromRequest(request);
        
        /**
         * 步骤3：验证Token是否有效（包含黑名单检查）
         * 
         * 如果Token为空或无效或已被拉黑，返回401未授权错误
         */
        JwtUtil.TokenValidationResult validation = jwtUtil.validateTokenWithReason(token);
        if (!validation.isValid()) {
            log.warn("请求未授权: path={}, tokenPresent={}, reason={}", requestPath, token != null, validation.getReasonForResponse());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            String reasonJson = "";
            if (jwtDebug) {
                String reason = validation.getReasonForResponse();
                if (reason != null) {
                    String escaped = reason.replace("\\", "\\\\").replace("\"", "\\\"");
                    reasonJson = ",\"reason\":\"" + escaped + "\"";
                }
            }
            response.getWriter().write("{\"error\":\"未授权：请先登录\"" + reasonJson + "}");
            return;
        }

        /**
         * 步骤4：从Token中提取用户信息
         * 
         * 提取userId和role，并存储到Request属性中
         * Controller可以通过request.getAttribute("userId")获取用户ID
         */
        Long userId = jwtUtil.getUserIdFromToken(token);
        Integer role = jwtUtil.getRoleFromToken(token);
        
        /**
         * 验证userId是否有效
         * 
         * 如果userId为null，说明Token解析失败，返回401错误
         */
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"未授权：Token无效\"}");
            return;
        }

        /**
         * 步骤5：将用户信息存储到Request属性中
         * 
         * Controller可以通过以下方式获取用户信息：
         * Long userId = (Long) request.getAttribute("userId");
         * Integer role = (Integer) request.getAttribute("role");
         */
        request.setAttribute("userId", userId);
        request.setAttribute("role", role);

        /**
         * 步骤6：对于管理员接口，检查用户角色
         * 
         * 如果请求的是管理员接口（/api/admin/*），但用户不是管理员（role != 2），返回403禁止访问
         */
        if (requestPath.startsWith("/api/admin/") && !requestPath.equals("/api/admin/login")) {
            /**
             * 检查用户角色是否为管理员（role = 2）
             */
            if (role == null || role != 2) {
                /**
                 * 设置HTTP状态码为403（禁止访问）
                 */
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"禁止访问：需要管理员权限\"}");
                return;
            }
        }

        /**
         * 步骤7：继续执行后续的过滤器或Controller
         * 
         * Token验证通过，用户信息已存储到Request中
         * 继续执行后续的处理流程
         */
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中获取Token
     * 
     * 支持两种格式：
     * 1. Authorization: Bearer <token>（标准格式）
     * 2. Authorization: <token>（简化格式）
     * 
     * @param request HTTP请求对象
     * @return Token字符串，如果不存在则返回null
     * 
     * 说明：
     * - getHeader("Authorization")：获取Authorization请求头的值
     * - Bearer是OAuth2标准中定义的Token类型标识
     * - 如果格式是"Bearer <token>"，需要去掉"Bearer "前缀
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        /**
         * 获取Authorization请求头的值
         */
        String bearerToken = request.getHeader("Authorization");
        
        /**
         * 检查Token格式
         */
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            /**
             * 格式：Authorization: Bearer <token>
             * 去掉"Bearer "前缀（7个字符），返回Token
             */
            return bearerToken.substring(7);
        } else if (StringUtils.hasText(bearerToken)) {
            /**
             * 格式：Authorization: <token>
             * 直接返回Token
             */
            return bearerToken;
        }
        
        /**
         * 没有找到Token，返回null
         */
        return null;
    }

    /**
     * 判断是否为排除路径
     * 
     * 用途：检查请求路径是否在排除列表中（不需要Token验证的路径）
     * 
     * @param path 请求路径
     * @return true表示是排除路径，false表示需要Token验证
     * 
     * 匹配规则：
     * - 精确匹配：path.equals(excludePath)
     * - 前缀匹配：path.startsWith(excludePath + "/")
     * 
     * 示例：
     * - /api/user/register-or-login -> true（精确匹配）
     * - /api/user/register-or-login/xxx -> true（前缀匹配）
     * - /api/user/current -> false（不在排除列表中）
     */
    private boolean isExcludePath(String path) {
        /**
         * 遍历排除路径列表
         */
        for (String excludePath : EXCLUDE_PATHS) {
            /**
             * 精确匹配或前缀匹配
             */
            if (path.equals(excludePath) || path.startsWith(excludePath + "/")) {
                return true;
            }
        }
        return false;
    }
}
