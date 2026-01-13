package com.mianzi.showticketsystem.service;

import com.mianzi.showticketsystem.model.entity.PageResult;
import com.mianzi.showticketsystem.model.entity.User;

/**
 * 用户业务逻辑接口
 */
public interface UserService {

    /**
     * 用户注册/登录方法（合并接口）
     * 如果用户不存在则注册，存在则登录
     * @param username 用户名或邮箱
     * @param password 密码
     * @param email 邮箱（注册时可选）
     * @return 返回一个完整的User对象（如果成功）或null（如果失败）
     */
    User registerOrLogin(String username, String password, String email);

    /**
     * 用户登录方法（支持用户名和邮箱登录）
     * @param account 用户名或邮箱
     * @param password 密码
     * @return 返回一个完整的User对象（如果登录成功）或null（如果登录失败）
     */
    User login(String account, String password);

    /**
     * 根据ID获取用户信息
     * @param id 用户ID
     * @return 用户对象
     */
    User getUserById(Long id);

    /**
     * 检查邮箱是否已被使用（不验证密码）
     * @param email 邮箱
     * @return 如果邮箱存在则返回用户对象，否则返回null
     */
    User checkEmailExists(String email);

    /**
     * 检查用户名是否存在（不验证密码）
     * @param username 用户名
     * @return 如果用户名存在则返回用户对象，否则返回null
     */
    User checkUsernameExists(String username);

    /**
     * 更新用户信息
     * @param user 用户对象
     * @return 成功返回 true，失败返回 false
     */
    boolean updateUser(User user);

    /**
     * 管理端 - 分页查询用户列表（条件查询）
     * @param username 用户名（可选，模糊查询）
     * @param status 状态（可选）
     * @param pageNum 当前页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    PageResult<User> getUserList(String username, Integer status, int pageNum, int pageSize);
}
