package com.mianzi.showticketsystem.model.dto;

import lombok.Data;

/**
 * 登录响应DTO
 */
@Data
public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private Integer role;
    private String message;

    public LoginResponse(String token, Long userId, String username, Integer role, String message) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.message = message;
    }
}

