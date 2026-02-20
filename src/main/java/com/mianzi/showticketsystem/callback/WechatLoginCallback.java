package com.mianzi.showticketsystem.callback;

import com.mianzi.showticketsystem.mapper.UserMapper;
import com.mianzi.showticketsystem.model.entity.User;
import com.mianzi.showticketsystem.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 微信登录回调处理类
 * 
 * 作用：
 * - 实现 WeixinLoginCallback 接口，处理微信扫码登录成功后的业务逻辑
 * - 当用户扫码并确认后，微信会调用此回调方法
 * - 根据 openid 查找或创建用户，生成 JWT Token
 * 
 * 工作流程：
 * 1. 用户扫码并确认
 * 2. 微信调用 onLoginSuccess(sceneId, openid)
 * 3. 根据 openid 查找用户，如果不存在则创建新用户
 * 4. 生成 JWT Token 并存储到 SCENE_TOKEN_MAP（key=sceneId）
 * 5. 前端通过 sceneId 查询登录状态，成功后调用 /api/user/wechat-token 获取 Token
 */
@Component
public class WechatLoginCallback implements com.tofries.wxlogin.callback.WeixinLoginCallback {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 临时存储：sceneId -> JWT Token
     * 
     * 说明：
     * - 用户扫码成功后，将 Token 存储在此 Map 中
     * - key 是 sceneId（场景值），value 是 JWT Token
     * - 前端通过 sceneId 查询登录状态成功后，调用 /api/user/wechat-token 获取 Token
     * - 获取后立即删除，避免重复使用
     * 
     * 注意：生产环境建议使用 Redis 替代内存 Map，支持分布式部署
     */
    private static final Map<String, String> SCENE_TOKEN_MAP = new ConcurrentHashMap<>();

    /**
     * 微信登录成功回调方法
     * 
     * 当用户扫码并确认后，微信会调用此方法
     * 
     * @param sceneId 场景值，用于标识本次登录请求
     * @param openid 微信用户的唯一标识
     * @return 返回给微信的消息（会显示给用户）
     */
    @Override
    public String onLoginSuccess(String sceneId, String openid) {
        // 步骤1：根据 openid 查找用户
        User user = userMapper.selectByWechatOpenid(openid);
        
        // 步骤2：如果用户不存在，创建新用户
        if (user == null) {
            LocalDateTime now = LocalDateTime.now();
            user = new User()
                    .setUsername("wx_" + openid.substring(0, Math.min(8, openid.length()))) // 用户名：wx_前8位openid
                    .setPassword(UUID.randomUUID().toString()) // 随机密码（微信登录用户不需要密码）
                    .setWechatOpenid(openid)
                    .setRole(1) // 默认角色：普通用户
                    .setStatus(1) // 默认状态：正常
                    .setCreateTime(now)
                    .setUpdateTime(now);
            
            // 插入新用户到数据库
            userMapper.insert(user);
        }
        
        // 步骤3：生成 JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        // 步骤4：将 Token 存储到 Map 中（key=sceneId）
        SCENE_TOKEN_MAP.put(sceneId, token);
        
        // 步骤5：返回成功消息（会显示给用户）
        return "登录成功！";
    }

    /**
     * 获取并删除 Token（供 Controller 调用）
     * 
     * 说明：
     * - 前端通过 sceneId 查询登录状态成功后，调用此方法获取 Token
     * - 获取后立即删除，避免重复使用
     * - 如果 sceneId 不存在，返回 null
     * 
     * @param sceneId 场景值
     * @return JWT Token，如果不存在则返回 null
     */
    public static String getAndRemoveToken(String sceneId) {
        return SCENE_TOKEN_MAP.remove(sceneId);
    }
}
