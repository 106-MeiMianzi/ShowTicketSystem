package com.mianzi.showticketsystem.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;

//Javadoc/类注释,说明这个类的作用是“用户实体类”，对应数据库中的user表
/**
 * 用户实体类 (对应数据库表 user)
 */
//Lombok注解
//告诉编译器自动为所有字段生成以下方法：
//Getter、Setter、equals()、hashCode()、canEqual()和toString()
@Data
//Lombok 注解,用于设置 Setter 方法的返回值
//设置为 chain = true 后，Setter 方法将返回对象本身 (this)，允许你使用链式调用
//例如 new User().setUsername("张三").setPassword("123")
@Accessors(chain = true)
public class User {

    /**
     * 用户ID（主键，自增）
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（存储加密后的哈希值）
     */
    private String password;

    /**
     * 邮箱（用于登录和接收通知）
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 角色（1:普通用户, 2:管理员）
     * 用于区分用户权限
     */
    private Integer role;

    /**
     * 账户状态（1:正常, 0:禁用）
     * 禁用对应管理端的“启用/禁用用户”功能
     */
    private Integer status;

    /**
     * 创建时间
     * 记录用户注册的时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     * 记录用户信息最后一次修改的时间
     */
    private LocalDateTime updateTime;

    //判断当前用户是否是管理员
    public boolean getIsAdmin() {
        //role = 2 表示管理员
        //检查 role 字段是否不为空，并且值等于 2
        return this.role != null && this.role.equals(2);
    }
}