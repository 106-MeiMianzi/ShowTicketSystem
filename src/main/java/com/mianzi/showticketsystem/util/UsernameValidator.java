package com.mianzi.showticketsystem.util;

import java.util.regex.Pattern;

/**
 * 用户名验证工具类
 * 用于验证用户名格式是否符合规范
 */
public class UsernameValidator {
    
    // 用户名规则：
    // 1. 长度：3-20个字符
    // 2. 允许字符：字母（a-z, A-Z）、数字（0-9）、下划线（_）、中划线（-）
    // 3. 首字符：必须是字母或数字，不能是下划线或中划线
    
    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 20;
    
    // 只允许字母、数字、下划线、中划线
    private static final Pattern ALLOWED_CHARS_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");
    
    // 首字符必须是字母或数字
    private static final Pattern FIRST_CHAR_PATTERN = Pattern.compile("^[a-zA-Z0-9]");
    
    /**
     * 验证用户名是否合法
     * 
     * @param username 待验证的用户名
     * @return 验证结果对象，包含是否合法和错误消息
     */
    public static ValidationResult validate(String username) {
        // 空值检查
        if (username == null || username.trim().isEmpty()) {
            return new ValidationResult(false, "用户名不能为空");
        }
        
        // 去除首尾空格
        username = username.trim();
        
        // 长度检查
        if (username.length() < MIN_LENGTH) {
            return new ValidationResult(false, 
                String.format("用户名长度不能少于%d个字符", MIN_LENGTH));
        }
        
        if (username.length() > MAX_LENGTH) {
            return new ValidationResult(false, 
                String.format("用户名长度不能超过%d个字符", MAX_LENGTH));
        }
        
        // 字符检查：只允许字母、数字、下划线、中划线
        if (!ALLOWED_CHARS_PATTERN.matcher(username).matches()) {
            return new ValidationResult(false, 
                "用户名只能包含字母、数字、下划线和中划线");
        }
        
        // 首字符检查：必须是字母或数字
        // 使用 find() 而不是 matches()，因为只需要检查首字符
        if (!FIRST_CHAR_PATTERN.matcher(username).find()) {
            return new ValidationResult(false, 
                "用户名必须以字母或数字开头");
        }
        
        // 验证通过
        return new ValidationResult(true, "用户名格式正确");
    }
    
    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        
        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
    }
}

