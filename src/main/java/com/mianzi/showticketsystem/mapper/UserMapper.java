package com.mianzi.showticketsystem.mapper;

import com.mianzi.showticketsystem.model.entity.User;
//导入 @Mapper 注解: 这是 MyBatis 的核心注解
//它告诉 Spring Boot，这是一个需要被 MyBatis 扫描、并为之创建代理实现类的接口
import org.apache.ibatis.annotations.Mapper;
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
    //SQL 查询语句，用于从 user 表中查找匹配 username 的记录
    //#{username} 是 MyBatis 的参数占位符，它会取方法参数 username 的值
    @Select("SELECT * FROM user WHERE username = #{username}")
    //返回一个 User 实体对象（如果查到）或 null（如果没查到）
    User selectByUsername(String username);

    /**
     * 插入新用户即注册功能
     *
     * @param user 用户对象
     * @return 插入成功的记录数 (1 或 0)
     */
    //接收一个 User 实体对象，将其数据插入数据库
    //返回的 int 是影响的行数（成功则为 1）
    int insert(User user);
    //这里的 insert 方法不会使用注解，我们将在 UserService 中手动调用它
    //或者我们会在后续使用 XML 文件来实现更复杂的插入操作
    //insert 方法通常不会使用注解
    //而是通过 XML 文件（resources/mapper目录下的UserMapper.xml 文件中来实现
    //以便编写更灵活和复杂的 SQL 语句
}
