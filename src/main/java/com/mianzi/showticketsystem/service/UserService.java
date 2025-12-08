package com.mianzi.showticketsystem.service;

import com.mianzi.showticketsystem.model.entity.User;
//Controller 依赖于 UserService 接口，而不是依赖于具体的实现类 (UserServiceImpl)
//这使得可以随时更换底层实现，而不用改动Controller层的代码
/**
 * 用户业务逻辑接口
 */
public interface UserService {

    /**
     * 用户注册方法
     * @param username 用户名
     * @param password 密码
     * @return 注册成功返回 true，失败返回 false
     * true 表示成功，false 表示失败，例如用户名已存在
     */
    boolean register(String username, String password);

    /**
     * 用户登录方法
     * @param username 用户名
     * @param password 密码
     * @return 返回一个完整的User对象（如果登录成功）或null（如果登录失败，例如密码错误）
     */
    User login(String username, String password); // <-- 正确地放在接口内部
}
