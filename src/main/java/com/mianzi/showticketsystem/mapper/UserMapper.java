package com.mianzi.showticketsystem.mapper;

import com.mianzi.showticketsystem.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户数据库操作接口（Mapper）
 * 
 * 作用：
 * - 定义用户相关的数据库操作方法
 * - MyBatis 会根据这些方法签名和注解自动生成实现类
 * - 通过 @Mapper 注解注册为 Spring Bean，可以在 Service 层注入使用
 * 
 * MyBatis 工作原理：
 * 1. 扫描 @Mapper 注解的接口
 * 2. 为接口生成代理实现类
 * 3. 根据方法上的 SQL 注解或 XML 映射文件执行 SQL
 * 4. 将查询结果映射为 Java 对象
 */
@Mapper
/**
 * @Mapper 注解说明：
 * 
 * MyBatis 的核心注解
 * 
 * 作用：
 * - 告诉 Spring Boot，这是一个 MyBatis Mapper 接口
 * - Spring Boot 会自动扫描并创建这个接口的代理实现类
 * - 将接口注册为 Spring Bean，可以在其他地方通过 @Autowired 注入
 * 
 * 等价于：
 * - 在配置类上使用 @MapperScan("com.mianzi.showticketsystem.mapper")
 * - 或者在每个 Mapper 接口上使用 @Mapper（推荐）
 */
public interface UserMapper {

    /**
     * 根据用户名查询用户
     * 
     * 用途：注册时检查用户名是否已存在
     * 
     * @param username 用户名
     * @return 匹配的用户对象，如果不存在则返回 null
     * 
     * SQL 说明：
     * - #{username} 是 MyBatis 的参数占位符
     * - MyBatis 会自动进行 SQL 注入防护（参数化查询）
     * - SELECT * 会查询所有字段，MyBatis 会自动映射到 User 对象
     */
    @Select("SELECT * FROM user WHERE username = #{username}")
    User selectByUsername(String username);

    /**
     * 根据邮箱查询用户
     * 
     * 用途：邮箱登录时查找用户
     * 
     * @param email 邮箱
     * @return 匹配的用户对象，如果不存在则返回 null
     */
    @Select("SELECT * FROM user WHERE email = #{email}")
    User selectByEmail(String email);

    /**
     * 根据ID查询用户
     * 
     * 用途：根据用户ID获取用户信息
     * 
     * @param id 用户ID
     * @return 用户对象，如果不存在则返回 null
     */
    @Select("SELECT * FROM user WHERE id = #{id}")
    User selectById(Long id);

    /**
     * 插入新用户（注册功能）
     * 
     * 说明：
     * - 这个方法没有使用 @Insert 注解，SQL 定义在 XML 文件中
     * - MyBatis 会在 resources/mapper/UserMapper.xml 中查找对应的 SQL
     * - XML 文件提供了更灵活的 SQL 编写方式（动态 SQL、复杂查询等）
     * 
     * @param user 用户对象，包含要插入的用户信息
     * @return 插入成功的记录数（1 表示成功，0 表示失败）
     * 
     * 注意：
     * - 如果 username 已存在，会抛出唯一约束异常
     * - 需要在 Service 层处理异常
     */
    int insert(User user);

    /**
     * 更新用户信息
     * 
     * 说明：
     * - SQL 定义在 XML 文件中（resources/mapper/UserMapper.xml）
     * - 通常使用动态 SQL，只更新非空字段
     * 
     * @param user 用户对象，包含要更新的字段（id 必须设置）
     * @return 影响的行数（1 表示成功更新，0 表示用户不存在）
     */
    int update(User user);

    /**
     * 管理端 - 分页查询用户列表（条件查询）
     * 
     * 用途：管理端用户列表页面，支持按用户名和状态筛选
     * 
     * @param username 用户名（可选），用于模糊查询，如果为 null 则不过滤
     * @param status 状态（可选），用于筛选，如果为 null 则不过滤
     * @param offset 偏移量，计算公式：(pageNum - 1) * pageSize
     * @param limit 每页数量，限制返回的记录数
     * @return 用户列表
     * 
     * @Param 注解说明：
     * - 当方法有多个参数时，必须使用 @Param 注解指定参数名
     * - XML 文件中使用 #{username}、#{status} 等引用参数
     * - 如果只有一个参数且是简单类型，可以不用 @Param
     */
    java.util.List<User> findUsers(@Param("username") String username,
                                   @Param("status") Integer status,
                                   @Param("offset") Integer offset,
                                   @Param("limit") Integer limit);

    /**
     * 管理端 - 统计用户总数（条件查询）
     * 
     * 用途：配合分页查询，计算符合条件的用户总数
     * 
     * @param username 用户名（可选），用于模糊查询
     * @param status 状态（可选），用于筛选
     * @return 符合条件的用户总数
     * 
     * 说明：
     * - 用于计算总页数：pages = total / pageSize
     * - 查询条件与 findUsers 方法保持一致
     */
    long countUsers(@Param("username") String username,
                   @Param("status") Integer status);
}
