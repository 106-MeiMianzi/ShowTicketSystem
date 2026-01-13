package com.mianzi.showticketsystem.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 用于生成和解析JWT Token
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:showticketsystem_secret_key_2024_this_is_a_very_long_secret_key_for_security}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 默认24小时（毫秒）
    private Long expiration;

    /**
     * 生成JWT Token
     * @param userId 用户ID
     * @param username 用户名
     * @param role 角色（1:普通用户, 2:管理员）
     * @return JWT Token字符串
     */
    public String generateToken(Long userId, String username, Integer role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        
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
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        } else if (userId instanceof Long) {
            return (Long) userId;
        }
        return null;
    }

    /**
     * 从Token中获取用户名
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * 从Token中获取角色
     * @param token JWT Token
     * @return 角色（1:普通用户, 2:管理员）
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
     * 从Token中解析Claims
     * @param token JWT Token
     * @return Claims对象
     */
    private Claims getClaimsFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验证Token是否有效
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims == null) {
                return false;
            }
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}

