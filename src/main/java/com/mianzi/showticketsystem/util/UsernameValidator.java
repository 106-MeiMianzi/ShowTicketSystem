package com.mianzi.showticketsystem.util;

import java.util.regex.Pattern;

/**
 * 用户名验证工具类
 * 
 * 作用：
 * - 用于验证用户名格式是否符合规范
 * - 提供统一的用户名验证规则
 * - 确保用户名符合业务要求
 * 
 * 用户名规则：
 * 1. 长度：3-20个字符
 * 2. 允许字符：字母（a-z, A-Z）、数字（0-9）、下划线（_）、中划线（-）
 * 3. 首字符：必须是字母或数字，不能是下划线或中划线
 * 
 * 使用示例：
 * UsernameValidator.ValidationResult result = UsernameValidator.validate("user123");
 * if (result.isValid()) {
 *     // 用户名格式正确
 * } else {
 *     // 用户名格式错误，显示 result.getMessage()
 * }
 */
public class UsernameValidator {
    
    /**
     * 最小长度：3个字符
     */
    private static final int MIN_LENGTH = 3;
    
    /**
     * 最大长度：20个字符
     */
    private static final int MAX_LENGTH = 20;
    
    /**
     * 允许字符的正则表达式
     * 
     * 说明：
     * - ^[a-zA-Z0-9_-]+$：只允许字母、数字、下划线、中划线
     * - ^：字符串开始
     * - [a-zA-Z0-9_-]：字符集（字母、数字、下划线、中划线）
     * - +：一个或多个
     * - $：字符串结束
     */
    private static final Pattern ALLOWED_CHARS_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");
    
    /**
     * 首字符的正则表达式
     * 
     * 说明：
     * - ^[a-zA-Z0-9]：首字符必须是字母或数字
     * - ^：字符串开始
     * - [a-zA-Z0-9]：字母或数字
     */
    private static final Pattern FIRST_CHAR_PATTERN = Pattern.compile("^[a-zA-Z0-9]");
    
    /**
     * 验证用户名是否合法
     * 
     * 验证步骤：
     * 1. 空值检查
     * 2. 长度检查（3-20字符）
     * 3. 字符检查（只允许字母、数字、下划线、中划线）
     * 4. 首字符检查（必须是字母或数字）
     * 
     * @param username 待验证的用户名
     * @return 验证结果对象，包含是否合法和错误消息
     * 
     * 返回值说明：
     * - ValidationResult.isValid()：true表示合法，false表示不合法
     * - ValidationResult.getMessage()：错误消息或成功消息
     */
    public static ValidationResult validate(String username) {
        /**
         * 步骤1：空值检查
         * 
         * 检查用户名是否为null或空字符串
         */
        if (username == null || username.trim().isEmpty()) {
            return new ValidationResult(false, "用户名不能为空");
        }
        
        /**
         * 步骤2：去除首尾空格
         * 
         * trim()：去除字符串首尾的空白字符
         */
        username = username.trim();
        
        /**
         * 步骤3：长度检查
         * 
         * 检查用户名长度是否在3-20字符之间
         */
        if (username.length() < MIN_LENGTH) {
            return new ValidationResult(false, 
                String.format("用户名长度不能少于%d个字符", MIN_LENGTH));
        }
        
        if (username.length() > MAX_LENGTH) {
            return new ValidationResult(false, 
                String.format("用户名长度不能超过%d个字符", MAX_LENGTH));
        }
        
        /**
         * 步骤4：字符检查
         * 
         * 检查用户名是否只包含允许的字符（字母、数字、下划线、中划线）
         * 
         * matcher(username)：创建匹配器
         * matches()：完全匹配（整个字符串必须匹配）
         */
        if (!ALLOWED_CHARS_PATTERN.matcher(username).matches()) {
            return new ValidationResult(false, 
                "用户名只能包含字母、数字、下划线和中划线");
        }
        
        /**
         * 步骤5：首字符检查
         * 
         * 检查首字符是否是字母或数字
         * 
         * find()：查找匹配（只要找到匹配就返回true）
         * 使用find()而不是matches()，因为只需要检查首字符
         */
        if (!FIRST_CHAR_PATTERN.matcher(username).find()) {
            return new ValidationResult(false, 
                "用户名必须以字母或数字开头");
        }
        
        /**
         * 所有验证通过
         */
        return new ValidationResult(true, "用户名格式正确");
    }
    
    /**
     * 验证结果类
     * 
     * 作用：
     * - 封装验证结果和错误消息
     * - 提供便捷的方法访问验证结果
     */
    public static class ValidationResult {
        /**
         * 是否合法
         * - true：用户名格式正确
         * - false：用户名格式错误
         */
        private final boolean valid;
        
        /**
         * 验证消息
         * - 如果合法：通常是"用户名格式正确"
         * - 如果不合法：包含具体的错误原因
         */
        private final String message;
        
        /**
         * 构造函数
         * 
         * @param valid 是否合法
         * @param message 验证消息
         */
        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        /**
         * 获取验证结果
         * 
         * @return true表示合法，false表示不合法
         */
        public boolean isValid() {
            return valid;
        }
        
        /**
         * 获取验证消息
         * 
         * @return 验证消息（错误原因或成功消息）
         */
        public String getMessage() {
            return message;
        }
    }
}
