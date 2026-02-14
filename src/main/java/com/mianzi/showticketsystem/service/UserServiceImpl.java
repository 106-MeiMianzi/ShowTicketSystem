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
 * 
 * 作用：
 * - 实现UserService接口定义的所有业务逻辑方法
 * - 调用Mapper层进行数据库操作
 * - 处理业务逻辑和异常情况
 * 
 * 职责：
 * - 用户注册、登录逻辑
 * - 用户信息查询和更新
 * - 用户列表分页查询
 */
@Service
/**
 * @Service 注解说明：
 * 
 * Spring提供的服务层注解
 * 
 * 作用：
 * - 标识这是一个Service层的Bean
 * - Spring会自动扫描并创建这个类的实例
 * - 可以在Controller层通过@Autowired注入使用
 * - 单例模式，整个应用只有一个实例
 */
public class UserServiceImpl implements UserService {

    /**
     * 注入用户Mapper
     * 
     * @Autowired 注解：
     * - 自动注入UserMapper的实现类（MyBatis自动生成）
     * - 用于执行数据库操作
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * 实现用户注册/登录方法（合并接口）
     * 
     * 业务逻辑：
     * 1. 先根据用户名查找用户
     * 2. 如果用户不存在，则注册新用户
     * 3. 如果用户存在，则验证密码并登录
     * 
     * @param username 用户名
     * @param password 密码
     * @param email 邮箱（可选）
     * @return User对象（成功）或null（失败）
     */
    @Override
    public User registerOrLogin(String username, String password, String email) {
        /**
         * 步骤1：先尝试根据用户名查找用户
         */
        User user = userMapper.selectByUsername(username);
        
        /**
         * 步骤2：如果用户不存在，则注册新用户
         */
        if (user == null) {
            /**
             * 步骤2.1：检查邮箱是否已被使用（如果提供了邮箱）
             */
            if (email != null && !email.isEmpty()) {
                User emailUser = userMapper.selectByEmail(email);
                if (emailUser != null) {
                    /**
                     * 邮箱已被使用，注册失败
                     */
                    return null;
                }
            }

            /**
             * 步骤2.2：创建新用户对象
             * 
             * 使用链式调用设置属性
             */
            LocalDateTime now = LocalDateTime.now();
            user = new User()
                    .setUsername(username)
                    .setPassword(password)  // 注意：实际项目中应该加密密码
                    .setEmail(email)
                    .setRole(1)             // 默认角色为普通用户
                    .setStatus(1)           // 默认状态为正常
                    .setCreateTime(now)
                    .setUpdateTime(now);

            /**
             * 步骤2.3：插入新用户到数据库
             */
            int result = userMapper.insert(user);
            if (result == 1) {
                /**
                 * 注册成功，返回用户信息
                 * 
                 * 注意：user.getId()会返回数据库生成的主键ID
                 */
                return user;
            } else {
                /**
                 * 注册失败（数据库操作失败）
                 */
                return null;
            }
        } else {
            /**
             * 步骤3：用户存在，执行登录逻辑
             */
            /**
             * 步骤3.1：验证密码
             * 
             * 注意：实际项目中应该使用加密后的密码比较
             * 这里简化处理，直接比较明文密码
             */
            if (user.getPassword().equals(password)) {
                /**
                 * 密码正确，登录成功
                 */
                return user;
            } else {
                /**
                 * 密码错误，登录失败
                 */
                return null;
            }
        }
    }

    /**
     * 实现用户登录方法（支持用户名和邮箱登录）
     * 
     * 业务逻辑：
     * 1. 判断是邮箱还是用户名
     * 2. 根据账号类型查询用户
     * 3. 检查用户状态
     * 4. 验证密码
     * 
     * @param account 用户名或邮箱
     * @param password 密码
     * @return User对象（成功）或null（失败）
     */
    @Override
    public User login(String account, String password) {
        User user = null;
        
        /**
         * 步骤1：判断是邮箱还是用户名（简单判断：包含@符号则为邮箱）
         */
        if (account != null && account.contains("@")) {
            /**
             * 邮箱登录：根据邮箱查询用户
             */
            user = userMapper.selectByEmail(account);
        } else {
            /**
             * 用户名登录：根据用户名查询用户
             */
            user = userMapper.selectByUsername(account);
        }

        /**
         * 步骤2：如果用户不存在，登录失败
         */
        if (user == null) {
            return null;
        }

        /**
         * 步骤3：检查用户状态
         * 
         * status = 0 表示用户被禁用，无法登录
         */
        if (user.getStatus() == null || user.getStatus() == 0) {
            /**
             * 用户被禁用，登录失败
             */
            return null;
        }

        /**
         * 步骤4：校验密码
         * 
         * 注意：实际项目中应该使用加密后的密码比较
         */
        if (user.getPassword().equals(password)) {
            /**
             * 密码正确，登录成功
             */
            return user;
        } else {
            /**
             * 密码错误，登录失败
             */
            return null;
        }
    }

    /**
     * 实现根据ID获取用户信息的逻辑
     * 
     * @param id 用户ID
     * @return 用户对象，如果不存在则返回null
     */
    @Override
    public User getUserById(Long id) {
        /**
         * 直接调用Mapper层查询用户
         */
        return userMapper.selectById(id);
    }

    /**
     * 检查邮箱是否已被使用（不验证密码）
     * 
     * 用途：注册时检查邮箱是否已被其他用户使用
     * 
     * @param email 邮箱
     * @return 如果邮箱存在则返回用户对象，否则返回null
     */
    @Override
    public User checkEmailExists(String email) {
        /**
         * 空值检查
         */
        if (email == null || email.isEmpty()) {
            return null;
        }
        /**
         * 调用Mapper层查询邮箱
         */
        return userMapper.selectByEmail(email);
    }

    /**
     * 检查用户名是否存在（不验证密码）
     * 
     * 用途：注册时检查用户名是否已被使用
     * 
     * @param username 用户名
     * @return 如果用户名存在则返回用户对象，否则返回null
     */
    @Override
    public User checkUsernameExists(String username) {
        /**
         * 空值检查
         */
        if (username == null || username.isEmpty()) {
            return null;
        }
        /**
         * 调用Mapper层查询用户名
         */
        return userMapper.selectByUsername(username);
    }

    /**
     * 实现更新用户信息的逻辑
     * 
     * 功能说明：
     * - 更新用户的个人信息
     * - 可以更新部分字段（只更新非空字段）
     * - 自动更新updateTime
     * 
     * @param user 用户对象，必须包含id，其他字段可选
     * @return 成功返回true，失败返回false
     */
    @Override
    public boolean updateUser(User user) {
        /**
         * 设置更新时间
         */
        user.setUpdateTime(LocalDateTime.now());
        
        /**
         * 调用Mapper层更新用户信息
         * 
         * result = 1 表示更新成功（影响1行）
         * result = 0 表示更新失败（用户不存在）
         */
        int result = userMapper.update(user);
        return result == 1;
    }

    /**
     * 实现管理端分页查询用户列表的逻辑
     * 
     * 功能说明：
     * - 支持按用户名模糊查询
     * - 支持按状态筛选
     * - 支持分页查询
     * 
     * @param username 用户名（可选），用于模糊查询
     * @param status 状态（可选），1=正常，0=禁用
     * @param pageNum 当前页码（从1开始）
     * @param pageSize 每页数量
     * @return PageResult对象，包含用户列表和分页信息
     */
    @Override
    public PageResult<User> getUserList(String username, Integer status, int pageNum, int pageSize) {
        /**
         * 步骤1：参数校验和修正
         */
        if (pageNum <= 0) pageNum = 1;      // 页码最小为1
        if (pageSize <= 0) pageSize = 10;   // 每页数量最小为10
        
        /**
         * 页大小上限限制，防止恶意请求导致数据库压力过大
         * 
         * 如果pageSize超过100，限制为100
         */
        if (pageSize > 100) pageSize = 100;

        /**
         * 步骤2：计算偏移量
         * 
         * offset = (pageNum - 1) * pageSize
         * 例如：第2页，每页10条 -> offset = 10（跳过前10条）
         */
        int offset = (pageNum - 1) * pageSize;

        /**
         * 步骤3：查询总记录数
         * 
         * 用于计算总页数
         */
        long total = userMapper.countUsers(username, status);

        /**
         * 步骤4：如果总记录数为0，直接返回空结果
         * 
         * 避免不必要的数据库查询
         */
        if (total == 0) {
            return PageResult.build(0, pageNum, pageSize, List.of());
        }

        /**
         * 步骤5：分页查询列表数据
         * 
         * 查询指定页码的用户列表
         */
        List<User> records = userMapper.findUsers(username, status, offset, pageSize);

        /**
         * 步骤6：封装为PageResult并返回
         * 
         * PageResult.build()会自动计算总页数
         */
        return PageResult.build(total, pageNum, pageSize, records);
    }
}
