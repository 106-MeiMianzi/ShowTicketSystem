package com.mianzi.showticketsystem.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 
 * 对应数据库表：user
 * 
 * 作用：
 * - 映射数据库中的用户表结构
 * - 用于在 Java 代码中表示用户数据
 * - 通过 MyBatis 实现数据库记录与 Java 对象的自动映射
 * 
 * 使用 Lombok 注解自动生成常用方法，减少样板代码
 */
@Data
/**
 * @Data 注解说明：
 * 
 * Lombok 提供的组合注解，自动生成以下方法：
 * - getter 方法：getId(), getUsername(), getPassword() 等
 * - setter 方法：setId(), setUsername(), setPassword() 等
 * - equals() 方法：用于对象比较
 * - hashCode() 方法：用于哈希表（如 HashMap）的键值计算
 * - toString() 方法：用于打印对象信息
 * 
 * 等价于手动编写所有这些方法，大大简化代码
 */
@Accessors(chain = true)
/**
 * @Accessors(chain = true) 注解说明：
 * 
 * 启用链式调用模式
 * 
 * 设置为 chain = true 后，Setter 方法将返回对象本身（this）
 * 允许使用链式调用，代码更简洁优雅
 * 
 * 示例：
 * // 不使用链式调用（需要多行）
 * User user = new User();
 * user.setUsername("张三");
 * user.setPassword("123456");
 * 
 * // 使用链式调用（一行搞定）
 * User user = new User().setUsername("张三").setPassword("123456");
 */
public class User {

    /**
     * 用户ID（主键，自增）
     * 
     * 数据库字段：id BIGINT AUTO_INCREMENT PRIMARY KEY
     * 
     * 特点：
     * - 主键，唯一标识一个用户
     * - 自增，数据库自动生成
     * - 类型为 Long，支持大数值
     */
    private Long id;

    /**
     * 用户名
     * 
     * 数据库字段：username VARCHAR(50) NOT NULL UNIQUE
     * 
     * 特点：
     * - 唯一约束，不能重复
     * - 用于登录和显示
     * - 最大长度 50 字符
     */
    private String username;

    /**
     * 密码（存储加密后的哈希值）
     * 
     * 数据库字段：password VARCHAR(255) NOT NULL
     * 
     * 安全说明：
     * - 不应该存储明文密码
     * - 应该存储加密后的哈希值（如 BCrypt、MD5、SHA-256 等）
     * - 最大长度 255 字符，足够存储各种加密算法的结果
     */
    private String password;

    /**
     * 邮箱（用于登录和接收通知）
     * 
     * 数据库字段：email VARCHAR(100)
     * 
     * 用途：
     * - 可以作为登录账号（支持邮箱登录）
     * - 用于接收系统通知（如订单确认、密码重置等）
     * - 可选字段，可以为空
     */
    private String email;

    /**
     * 手机号
     * 
     * 数据库字段：phone VARCHAR(20)
     * 
     * 用途：
     * - 联系方式
     * - 可用于接收短信验证码
     * - 可选字段，可以为空
     */
    private String phone;

    /**
     * 真实姓名
     * 
     * 数据库字段：real_name VARCHAR(50)
     * 
     * 注意：
     * - 数据库使用下划线命名（real_name）
     * - Java 使用驼峰命名（realName）
     * - MyBatis 会自动转换（通过 map-underscore-to-camel-case 配置）
     */
    private String realName;

    /**
     * 角色（1:普通用户, 2:管理员）
     * 
     * 数据库字段：role INT DEFAULT 1
     * 
     * 角色说明：
     * - 1：普通用户，只能使用用户端功能
     * - 2：管理员，可以使用管理端功能
     * 
     * 用于权限控制，区分用户和管理员的访问权限
     */
    private Integer role;

    /**
     * 账户状态（1:正常, 0:禁用）
     * 
     * 数据库字段：status INT DEFAULT 1
     * 
     * 状态说明：
     * - 1：正常，用户可以正常登录和使用系统
     * - 0：禁用，用户无法登录，账号被冻结
     * 
     * 用途：
     * - 管理端可以启用/禁用用户账号
     * - 登录时需要检查状态，禁用账号无法登录
     */
    private Integer status;

    /**
     * 创建时间
     * 
     * 数据库字段：create_time DATETIME DEFAULT CURRENT_TIMESTAMP
     * 
     * 说明：
     * - 记录用户注册的时间
     * - 数据库自动设置，插入记录时自动填充当前时间
     * - 使用 LocalDateTime 类型，Java 8+ 的时间 API
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     * 
     * 数据库字段：update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
     * 
     * 说明：
     * - 记录用户信息最后一次修改的时间
     * - 数据库自动更新，每次更新记录时自动更新为当前时间
     * - 用于追踪数据变更历史
     */
    private LocalDateTime updateTime;

    /**
     * 微信 openid（用于微信扫码登录）
     * 
     * 数据库字段：wechat_openid VARCHAR(64) UNIQUE
     * 
     * 说明：
     * - 微信用户的唯一标识
     * - 用于微信扫码登录时识别用户
     * - 唯一约束，一个 openid 对应一个用户
     * - 可选字段，只有通过微信登录的用户才有此值
     */
    private String wechatOpenid;

    /**
     * 判断当前用户是否是管理员
     * 
     * @return true 如果是管理员（role = 2），false 否则
     * 
     * 方法说明：
     * - 这是一个便捷方法，用于快速判断用户角色
     * - 避免在业务代码中重复写 role != null && role.equals(2)
     * 
     * 使用示例：
     * User user = userService.getUserById(1L);
     * if (user.getIsAdmin()) {
     *     // 执行管理员操作
     * }
     */
    public boolean getIsAdmin() {
        /**
         * 判断逻辑：
         * 1. this.role != null：先检查 role 字段不为空，避免空指针异常
         * 2. this.role.equals(2)：再检查 role 值是否等于 2（管理员）
         * 
         * 注意：使用 equals() 而不是 ==，因为 Integer 是对象类型
         * 虽然对于小整数（-128 到 127）Java 会缓存，但使用 equals() 更安全
         */
        return this.role != null && this.role.equals(2);
    }
}
