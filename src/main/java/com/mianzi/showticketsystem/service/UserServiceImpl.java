package com.mianzi.showticketsystem.service;

import com.mianzi.showticketsystem.mapper.UserMapper;
import com.mianzi.showticketsystem.model.entity.PageResult;
import com.mianzi.showticketsystem.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * UserService 接口的实现类
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 实现用户注册/登录方法（合并接口）
     */
    @Override
    public User registerOrLogin(String username, String password, String email) {
        // 先尝试根据用户名查找
        User user = userMapper.selectByUsername(username);
        
        // 如果用户不存在，则注册
        if (user == null) {
            // 检查邮箱是否已被使用（如果提供了邮箱）
            if (email != null && !email.isEmpty()) {
                User emailUser = userMapper.selectByEmail(email);
                if (emailUser != null) {
                    return null; // 邮箱已被使用
                }
            }

            // 创建新用户
            LocalDateTime now = LocalDateTime.now();
            user = new User()
                    .setUsername(username)
                    .setPassword(password)
                    .setEmail(email)
                    .setRole(1) // 默认角色为普通用户
                    .setStatus(1) // 默认状态为正常
                    .setCreateTime(now)
                    .setUpdateTime(now);

            int result = userMapper.insert(user);
            if (result == 1) {
                return user; // 注册成功，返回用户信息
            } else {
                return null; // 注册失败
            }
        } else {
            // 用户存在，执行登录逻辑
            if (user.getPassword().equals(password)) {
                return user; // 登录成功
            } else {
                return null; // 密码错误
            }
        }
    }

    /**
     * 实现用户登录方法（支持用户名和邮箱登录）
     */
    @Override
    public User login(String account, String password) {
        User user = null;
        
        // 判断是邮箱还是用户名（简单判断：包含@符号则为邮箱）
        if (account != null && account.contains("@")) {
            user = userMapper.selectByEmail(account);
        } else {
            user = userMapper.selectByUsername(account);
        }

        // 如果用户不存在，登录失败
        if (user == null) {
            return null;
        }

        // 检查用户状态
        if (user.getStatus() == null || user.getStatus() == 0) {
            return null; // 用户被禁用
        }

        // 校验密码
        if (user.getPassword().equals(password)) {
            return user; // 登录成功
        } else {
            return null; // 密码错误
        }
    }

    /**
     * 实现根据ID获取用户信息的逻辑
     */
    @Override
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    /**
     * 检查邮箱是否已被使用（不验证密码）
     */
    @Override
    public User checkEmailExists(String email) {
        if (email == null || email.isEmpty()) {
            return null;
        }
        return userMapper.selectByEmail(email);
    }

    /**
     * 检查用户名是否存在（不验证密码）
     */
    @Override
    public User checkUsernameExists(String username) {
        if (username == null || username.isEmpty()) {
            return null;
        }
        return userMapper.selectByUsername(username);
    }

    /**
     * 实现更新用户信息的逻辑
     */
    @Override
    public boolean updateUser(User user) {
        user.setUpdateTime(LocalDateTime.now());
        int result = userMapper.update(user);
        return result == 1;
    }

    /**
     * 实现管理端分页查询用户列表的逻辑
     */
    @Override
    public PageResult<User> getUserList(String username, Integer status, int pageNum, int pageSize) {
        // 参数校验
        if (pageNum <= 0) pageNum = 1;
        if (pageSize <= 0) pageSize = 10;
        // 页大小上限限制，防止恶意请求导致数据库压力过大
        if (pageSize > 100) pageSize = 100;

        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;

        // 查询总记录数
        long total = userMapper.countUsers(username, status);

        // 如果总记录数为 0，直接返回空结果
        if (total == 0) {
            return PageResult.build(0, pageNum, pageSize, List.of());
        }

        // 分页查询列表数据
        List<User> records = userMapper.findUsers(username, status, offset, pageSize);

        // 封装为 PageResult 并返回
        return PageResult.build(total, pageNum, pageSize, records);
    }
}
