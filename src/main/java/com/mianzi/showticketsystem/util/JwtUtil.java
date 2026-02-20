package com.mianzi.showticketsystem.util;

import com.mianzi.showticketsystem.util.RedisUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 
 * 作用：
 * - 用于生成和解析JWT Token
 * - 提供Token的生成、验证、解析等功能
 * - 实现用户身份认证的核心功能
 * 
 * JWT（JSON Web Token）说明：
 * - 一种无状态的认证方式，不需要在服务器端存储Session
 * - Token包含用户信息（userId、username、role等）
 * - Token有过期时间，过期后需要重新登录
 * - Token使用密钥签名，防止被篡改
 */
@Component
/**
 * @Component 注解说明：
 * 
 * 标识这是一个Spring组件（Bean）
 * 
 * 作用：
 * - Spring会自动扫描并创建这个类的实例
 * - 可以在其他地方通过 @Autowired 注入使用
 * - 单例模式，整个应用只有一个实例
 */
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    /**
     * JWT密钥
     * 
     * @Value 注解说明：
     * - 从 application.properties 中读取配置值
     * - ${jwt.secret:默认值} 表示如果配置文件中没有 jwt.secret，则使用默认值
     * 
     * 安全说明：
     * - 密钥应该足够长且随机，生产环境必须修改
     * - 密钥泄露会导致Token可以被伪造
     * - 建议使用环境变量或配置中心管理密钥
     */
    @Value("${jwt.secret:showticketsystem_secret_key_2024_this_is_a_very_long_secret_key_for_security}")
    private String secret;

    /**
     * Token过期时间（毫秒）
     * 
     * @Value 注解说明：
     * - 从 application.properties 中读取配置值
     * - ${jwt.expiration:86400000} 表示默认24小时（86400000毫秒）
     * 
     * 说明：
     * - 86400000 毫秒 = 24 小时
     * - 可以根据业务需求调整过期时间
     * - 过期时间过长会增加安全风险，过短会影响用户体验
     */
    @Value("${jwt.expiration:86400000}") // 默认24小时（毫秒）
    private Long expiration;

    /**
     * 注入Redis工具类
     * 
     * 用于Token黑名单管理
     */
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 生成JWT Token
     * 
     * 用途：用户登录成功后生成Token
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param role 角色（1:普通用户, 2:管理员）
     * @return JWT Token字符串
     * 
     * Token结构说明：
     * - Header（头部）：包含算法类型（如HS256）
     * - Payload（载荷）：包含用户信息（userId、username、role等）
     * - Signature（签名）：使用密钥对Header和Payload签名，防止篡改
     * 
     * 使用示例：
     * String token = jwtUtil.generateToken(1L, "张三", 1);
     * // 返回类似：eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     */
    public String generateToken(Long userId, String username, Integer role) {
        /**
         * 创建Claims（声明），存储用户信息
         * 
         * Claims是JWT的载荷部分，包含要传递的数据
         */
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);      // 用户ID
        claims.put("username", username);   // 用户名
        claims.put("role", role);           // 角色
        
        /**
         * 设置Token的时间信息
         */
        Date now = new Date();                                    // 当前时间
        Date expiryDate = new Date(now.getTime() + expiration);   // 过期时间 = 当前时间 + 过期时长

        /**
         * 生成签名密钥
         * 
         * 使用HMAC-SHA算法生成密钥
         * - 密钥长度必须至少256位（32字节）
         * - 使用UTF-8编码将字符串转换为字节数组
         */
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        
        /**
         * 构建并生成JWT Token
         * 
         * Jwts.builder()：创建JWT构建器
         * .claims(claims)：设置载荷（用户信息）
         * .subject(username)：设置主题（通常是用户名）
         * .issuedAt(now)：设置签发时间
         * .expiration(expiryDate)：设置过期时间
         * .signWith(key)：使用密钥签名
         * .compact()：生成最终的Token字符串
         */
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * 从Token中获取用户ID
     * 
     * 用途：从Token中提取用户ID，用于后续业务操作
     * 
     * @param token JWT Token
     * @return 用户ID，如果Token无效则返回 null
     * 
     * 说明：
     * - 先解析Token获取Claims
     * - 从Claims中提取userId字段
     * - 处理类型转换（Integer转Long）
     */
    public Long getUserIdFromToken(String token) {
        /**
         * 解析Token获取Claims
         */
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        
        /**
         * 从Claims中获取userId
         * 
         * 注意：JWT库可能将数字存储为Integer或Long
         * 需要处理类型转换
         */
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            // Integer转Long
            return ((Integer) userId).longValue();
        } else if (userId instanceof Long) {
            // 直接返回Long
            return (Long) userId;
        }
        return null;
    }

    /**
     * 从Token中获取用户名
     * 
     * 用途：从Token中提取用户名
     * 
     * @param token JWT Token
     * @return 用户名，如果Token无效则返回 null
     * 
     * 说明：
     * - 用户名存储在Token的subject字段中
     * - 使用getSubject()方法获取
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        /**
         * getSubject()：获取Token的主题，通常是用户名
         */
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * 从Token中获取角色
     * 
     * 用途：从Token中提取用户角色，用于权限控制
     * 
     * @param token JWT Token
     * @return 角色（1:普通用户, 2:管理员），如果Token无效则返回 null
     */
    public Integer getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        Object role = claims.get("role");
        if (role instanceof Integer) {
            return (Integer) role;
        }
        return null;
    }

    /**
     * 从Token中解析Claims（私有方法）
     * 
     * 用途：解析Token并获取Claims对象
     * 
     * @param token JWT Token
     * @return Claims对象，如果Token无效或过期则返回 null
     * 
     * 说明：
     * - 这是内部方法，其他方法都通过它来解析Token
     * - 使用try-catch捕获异常，Token无效时返回null
     * - 验证签名和过期时间
     */
    private Claims getClaimsFromToken(String token) {
        try {
            /**
             * 生成签名密钥（与生成Token时使用相同的密钥）
             */
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            
            /**
             * 解析并验证Token
             * 
             * Jwts.parser()：创建解析器
             * .verifyWith(key)：设置验证密钥
             * .build()：构建解析器
             * .parseSignedClaims(token)：解析Token
             * .getPayload()：获取载荷（Claims）
             * 
             * 验证过程：
             * 1. 验证签名：确保Token未被篡改
             * 2. 验证过期时间：确保Token未过期
             * 3. 如果验证失败，会抛出异常
             */
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (SignatureException e) {
            log.warn("JWT 签名无效（Secret 不一致或 Token 被篡改）: {}", e.getMessage());
            return null;
        } catch (ExpiredJwtException e) {
            log.warn("JWT 已过期: exp={}", e.getClaims().getExpiration());
            return null;
        } catch (Exception e) {
            log.warn("JWT 解析失败: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    /**
     * 解析 Token 并返回失败原因（用于调试时在 401 响应中返回具体原因）
     */
    private ClaimsParseResult getClaimsFromTokenWithReason(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return new ClaimsParseResult(claims, null);
        } catch (SignatureException e) {
            log.warn("JWT 签名无效（Secret 不一致或 Token 被篡改）: {}", e.getMessage());
            return new ClaimsParseResult(null, "SIGNATURE_INVALID");
        } catch (ExpiredJwtException e) {
            log.warn("JWT 已过期: exp={}", e.getClaims().getExpiration());
            return new ClaimsParseResult(null, "EXPIRED");
        } catch (Exception e) {
            log.warn("JWT 解析失败: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return new ClaimsParseResult(null, "PARSE_ERROR");
        }
    }

    /**
     * 校验 Token 并返回结果与失败原因（仅在 jwt.debug=true 时在 401 响应中返回 reason，便于排查）
     */
    public TokenValidationResult validateTokenWithReason(String token) {
        if (token == null || token.isBlank()) {
            return new TokenValidationResult(false, "NO_TOKEN");
        }
        try {
            if (isTokenBlacklisted(token)) {
                log.warn("JWT 校验失败: Token 已在黑名单中（可能已登出）");
                return new TokenValidationResult(false, "BLACKLISTED");
            }
        } catch (Exception e) {
            // Redis连接失败时，降级处理：跳过黑名单检查，继续JWT校验
            log.warn("Redis连接失败，跳过黑名单检查: {}", e.getMessage());
        }
        ClaimsParseResult parseResult = getClaimsFromTokenWithReason(token);
        if (parseResult.claims == null) {
            return new TokenValidationResult(false, parseResult.failureReason);
        }
        if (parseResult.claims.getExpiration().before(new Date())) {
            return new TokenValidationResult(false, "EXPIRED");
        }
        return new TokenValidationResult(true, null);
    }

    private static class ClaimsParseResult {
        final Claims claims;
        final String failureReason;

        ClaimsParseResult(Claims claims, String failureReason) {
            this.claims = claims;
            this.failureReason = failureReason;
        }
    }

    /**
     * 校验结果（含失败原因，用于调试）
     */
    public static class TokenValidationResult {
        private final boolean valid;
        private final String reason;

        public TokenValidationResult(boolean valid, String reason) {
            this.valid = valid;
            this.reason = reason;
        }

        public boolean isValid() {
            return valid;
        }

        /**
         * 返回对用户/调试友好的中文原因，仅在校验失败时有值
         */
        public String getReasonForResponse() {
            if (reason == null) {
                return null;
            }
            return switch (reason) {
                case "NO_TOKEN" -> "未携带Token";
                case "BLACKLISTED" -> "Token已登出或已失效";
                case "SIGNATURE_INVALID" -> "签名无效（多环境密钥不一致或Token被篡改）";
                case "EXPIRED" -> "Token已过期";
                case "PARSE_ERROR" -> "Token解析失败";
                default -> reason;
            };
        }
    }

    /**
     * 验证Token是否有效（包含黑名单检查）
     * 
     * 用途：检查Token是否有效（未过期、签名正确、未在黑名单中）
     * 
     * @param token JWT Token
     * @return true表示Token有效，false表示Token无效或已过期或已被拉黑
     * 
     * 验证内容：
     * 1. Token格式是否正确
     * 2. 签名是否正确（防止篡改）
     * 3. 是否已过期
     * 4. 是否在黑名单中
     */
    public boolean validateToken(String token) {
        try {
            /**
             * 步骤1：检查Token是否在黑名单中
             */
            if (isTokenBlacklisted(token)) {
                log.warn("JWT 校验失败: Token 已在黑名单中（可能已登出）");
                return false;
            }

            /**
             * 步骤2：解析Token获取Claims
             */
            Claims claims = getClaimsFromToken(token);
            if (claims == null) {
                return false;
            }
            
            /**
             * 步骤3：检查Token是否已过期
             * 
             * getExpiration()：获取过期时间
             * before(new Date())：检查是否在当前时间之前（已过期）
             * !claims.getExpiration().before(...)：未过期返回true
             */
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.warn("JWT 校验异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 将Token加入黑名单（用户登出时调用）
     * 
     * @param token JWT Token
     */
    public void addTokenToBlacklist(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims != null) {
                Date expiration = claims.getExpiration();
                long ttl = expiration.getTime() - System.currentTimeMillis();
                if (ttl > 0) {
                    // 将Token加入黑名单，TTL设置为Token的剩余过期时间
                    String blacklistKey = "blacklist:token:" + token;
                    redisUtil.set(blacklistKey, "1", ttl / 1000); // 转换为秒
                }
            }
        } catch (Exception e) {
            // 忽略异常，Token可能已过期或无效
        }
    }

    /**
     * 检查Token是否在黑名单中
     * 
     * @param token JWT Token
     * @return true表示Token已被拉黑，false表示Token未被拉黑或Redis不可用
     * 
     * 说明：Redis连接失败时返回false（降级处理），不阻止JWT校验，仅记录警告日志
     */
    public boolean isTokenBlacklisted(String token) {
        try {
            String blacklistKey = "blacklist:token:" + token;
            return Boolean.TRUE.equals(redisUtil.hasKey(blacklistKey));
        } catch (Exception e) {
            // Redis连接失败时，降级处理：假设Token不在黑名单中，不阻止JWT校验
            // 这样即使Redis不可用，系统仍能正常工作（只是黑名单功能暂时失效）
            log.warn("Redis连接失败，跳过黑名单检查: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从Token中获取过期时间（用于黑名单TTL计算）
     * 
     * @param token JWT Token
     * @return 过期时间，如果Token无效则返回null
     */
    public Date getExpirationFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getExpiration() : null;
    }
}
