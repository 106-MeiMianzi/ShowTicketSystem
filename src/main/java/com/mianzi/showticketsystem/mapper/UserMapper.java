package com.mianzi.showticketsystem.mapper;

import com.mianzi.showticketsystem.model.entity.User;
//导入 @Mapper 注解: 这是 MyBatis 的核心注解
//它告诉 Spring Boot，这是一个需要被 MyBatis 扫描、并为之创建代理实现类的接口
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户数据库操作接口
 * MyBatis/MyBatis-Plus 的数据持久化接口
 */
@Mapper
public interface UserMapper {

    /**
     * 根据用户名查询用户，用于注册时检查用户名是否已存在
     *
     * @param username 用户名
     * @return 匹配的用户对象，如果不存在则返回 null
     */
    @Select("SELECT * FROM user WHERE username = #{username}")
    User selectByUsername(String username);

    /**
     * 根据邮箱查询用户，用于邮箱登录
     * @param email 邮箱
     * @return 匹配的用户对象，如果不存在则返回 null
     */
    @Select("SELECT * FROM user WHERE email = #{email}")
    User selectByEmail(String email);

    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户对象
     */
    @Select("SELECT * FROM user WHERE id = #{id}")
    User selectById(Long id);

    /**
     * 插入新用户即注册功能
     * @param user 用户对象
     * @return 插入成功的记录数 (1 或 0)
     */
    int insert(User user);

    /**
     * 更新用户信息
     * @param user 用户对象
     * @return 影响的行数 (1 表示成功)
     */
    int update(User user);

    /**
     * 管理端 - 分页查询用户列表（条件查询）
     * @param username 用户名（可选，用于模糊查询）
     * @param status 状态（可选，用于筛选）
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 用户列表
     */
    java.util.List<User> findUsers(@Param("username") String username,
                                   @Param("status") Integer status,
                                   @Param("offset") Integer offset,
                                   @Param("limit") Integer limit);

    /**
     * 管理端 - 统计用户总数（条件查询）
     * @param username 用户名（可选）
     * @param status 状态（可选）
     * @return 总数
     */
    long countUsers(@Param("username") String username,
                   @Param("status") Integer status);
}
