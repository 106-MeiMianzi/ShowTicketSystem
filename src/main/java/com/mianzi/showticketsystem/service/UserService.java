package com.mianzi.showticketsystem.service;

import com.mianzi.showticketsystem.model.entity.PageResult;
import com.mianzi.showticketsystem.model.entity.User;

/**
 * 用户业务逻辑接口
 * 
 * 作用：
 * - 定义用户相关的业务逻辑方法
 * - 提供用户注册、登录、查询、更新等功能
 * - 由UserServiceImpl实现具体的业务逻辑
 * 
 * 设计模式：
 * - 接口与实现分离，便于扩展和维护
 * - Controller层只依赖接口，不依赖具体实现
 */
public interface UserService {

    /**
     * 用户注册/登录方法（合并接口）
     * 
     * 功能说明：
     * - 如果用户不存在则注册，存在则登录
     * - 自动判断是注册还是登录
     * 
     * @param username 用户名或邮箱
     * @param password 密码
     * @param email 邮箱（注册时可选）
     * @return 返回一个完整的User对象（如果成功）或null（如果失败）
     * 
     * 业务逻辑：
     * 1. 先根据用户名查找用户
     * 2. 如果用户不存在，则注册新用户
     * 3. 如果用户存在，则验证密码并登录
     */
    User registerOrLogin(String username, String password, String email);

    /**
     * 用户登录方法（支持用户名和邮箱登录）
     * 
     * 功能说明：
     * - 支持用户名登录和邮箱登录
     * - 自动识别账号类型（包含@则为邮箱）
     * 
     * @param account 用户名或邮箱
     * @param password 密码
     * @return 返回一个完整的User对象（如果登录成功）或null（如果登录失败）
     * 
     * 登录失败的原因：
     * - 用户不存在
     * - 密码错误
     * - 用户被禁用（status = 0）
     */
    User login(String account, String password);

    /**
     * 根据ID获取用户信息
     * 
     * @param id 用户ID
     * @return 用户对象，如果不存在则返回null
     */
    User getUserById(Long id);

    /**
     * 检查邮箱是否已被使用（不验证密码）
     * 
     * 用途：注册时检查邮箱是否已被其他用户使用
     * 
     * @param email 邮箱
     * @return 如果邮箱存在则返回用户对象，否则返回null
     */
    User checkEmailExists(String email);

    /**
     * 检查用户名是否存在（不验证密码）
     * 
     * 用途：注册时检查用户名是否已被使用
     * 
     * @param username 用户名
     * @return 如果用户名存在则返回用户对象，否则返回null
     */
    User checkUsernameExists(String username);

    /**
     * 更新用户信息
     * 
     * 功能说明：
     * - 更新用户的个人信息（用户名、邮箱、手机号、真实姓名等）
     * - 可以更新部分字段（只更新非空字段）
     * 
     * @param user 用户对象，必须包含id，其他字段可选
     * @return 成功返回true，失败返回false
     */
    boolean updateUser(User user);

    /**
     * 管理端 - 分页查询用户列表（条件查询）
     * 
     * 功能说明：
     * - 支持按用户名模糊查询
     * - 支持按状态筛选（正常/禁用）
     * - 支持分页查询
     * 
     * @param username 用户名（可选），用于模糊查询
     * @param status 状态（可选），1=正常，0=禁用
     * @param pageNum 当前页码（从1开始）
     * @param pageSize 每页数量
     * @return 分页结果，包含用户列表和分页信息
     */
    PageResult<User> getUserList(String username, Integer status, int pageNum, int pageSize);
}
