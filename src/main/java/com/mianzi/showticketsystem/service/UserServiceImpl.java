package com.mianzi.showticketsystem.service;

import com.mianzi.showticketsystem.mapper.UserMapper;
import com.mianzi.showticketsystem.model.entity.User;
import com.mianzi.showticketsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * UserService 接口的实现类
 */
/*
标记这个类是一个 Spring Service 组件
当 Spring Boot 启动时，它会自动创建这个类的一个实例（Bean）
并放入 IOC 容器 中管理
以便其他组件（如 UserController）可以通过 依赖注入 (DI) 来使用它
 */
@Service
public class UserServiceImpl implements UserService {

    /*
    依赖注入注解: 告诉 Spring 框架，请自动找到一个 UserMapper 的实例
    并将其赋值给下面的 userMapper 字段。这就是 DI (依赖注入) 的核心体现
     */
    @Autowired
    //创建数据库对象,用于调用数据库操作方法
    private UserMapper userMapper;

    /**
     * 实现用户注册逻辑
     * @param username 用户名
     * @param password 密码
     * @return 注册成功返回 true，失败返回 false
     */
    @Override
    public boolean register(String username, String password) {
        // 1. 检查用户名是否已存在
        if (userMapper.selectByUsername(username) != null) {
            // 用户名已存在
            return false;
        }

        // 2. 创建用户实体对象
        //这里使用了 lombok 的 @Accessors(chain=true) 链式调用
        User user = new User()
                .setUsername(username)
                //实际项目中密码需要加密，此处简化处理
                .setPassword(password)
                .setRole(1) // 默认角色为普通用户 (1)
                .setStatus(1) // 默认状态为正常 (1)
                //使用了java.time.LocalDateTime类(用来表示日期和时间的类)
                //调用now();方法,获取当前系统运行时的日期和时间
                .setCreateTime(LocalDateTime.now())
                .setUpdateTime(LocalDateTime.now());

        // 3. 插入数据库
        int result = userMapper.insert(user);

        // 4. 返回结果
        return result == 1;
    }

    /**
     * 实现用户登录逻辑
     * @param username 用户名
     * @param password 密码
     * @return 返回一个完整的User对象（如果登录成功）或null（如果登录失败，例如密码错误）
     */
    //编译时检查,这是实现接口的方法
    @Override
    public User login(String username, String password) {
        // 1. 根据用户名查询用户，这个方法在 UserMapper中
        User user = userMapper.selectByUsername(username);

        // 2. 如果用户不存在，登录失败
        if (user == null) {
            return null;
        }

        // 3. 校验密码
        // 注意：实际项目中密码需要加密处理。这里为了简化流程，我们先进行明文比对
        if (user.getPassword().equals(password)) {
            // 4. 密码匹配，登录成功
            return user;
        } else {
            // 密码不匹配，登录失败
            return null;
        }
    }
}
