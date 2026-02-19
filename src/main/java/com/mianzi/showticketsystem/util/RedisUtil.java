package com.mianzi.showticketsystem.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 * 
 * 作用：
 * - 封装常用的Redis操作
 * - 提供缓存、分布式锁、计数器等功能
 * 
 * 说明：
 * - 统一管理Redis操作，便于维护
 * - 提供原子操作保证并发安全
 */
@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 设置缓存
     * 
     * @param key 键
     * @param value 值
     * @param timeout 过期时间（秒）
     */
    public void set(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 设置缓存（不过期）
     * 
     * @param key 键
     * @param value 值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 获取缓存
     * 
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取缓存（指定类型）
     * 
     * @param key 键
     * @param clazz 类型
     * @return 值
     */
    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        // RedisTemplate已经处理了序列化，直接转换类型即可
        if (clazz.isInstance(value)) {
            return clazz.cast(value);
        }
        // 如果类型不匹配，尝试用ObjectMapper转换
        return objectMapper.convertValue(value, clazz);
    }

    /**
     * 删除缓存
     * 
     * @param key 键
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 判断键是否存在
     * 
     * @param key 键
     * @return 是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置过期时间
     * 
     * @param key 键
     * @param timeout 过期时间（秒）
     */
    public void expire(String key, long timeout) {
        redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 原子递增
     * 
     * @param key 键
     * @param delta 增量
     * @return 递增后的值
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 原子递减
     * 
     * @param key 键
     * @param delta 减量
     * @return 递减后的值
     */
    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    /**
     * 获取分布式锁
     * 
     * @param key 锁的键
     * @param value 锁的值（用于标识锁的持有者）
     * @param timeout 锁的超时时间（秒）
     * @return 是否获取成功
     */
    public Boolean tryLock(String key, String value, long timeout) {
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, timeout, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(result);
    }

    /**
     * 释放分布式锁（使用Lua脚本保证原子性）
     * 
     * @param key 锁的键
     * @param value 锁的值
     * @return 是否释放成功
     */
    public Boolean releaseLock(String key, String value) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                        "return redis.call('del', KEYS[1]) " +
                        "else return 0 end";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        Long result = redisTemplate.execute(redisScript, Collections.singletonList(key), value);
        return result != null && result > 0;
    }

    /**
     * 添加到有序集合（用于排行榜）
     * 
     * @param key 键
     * @param member 成员
     * @param score 分数
     */
    public void zAdd(String key, String member, double score) {
        redisTemplate.opsForZSet().add(key, member, score);
    }

    /**
     * 增加有序集合成员的分数
     * 
     * @param key 键
     * @param member 成员
     * @param delta 增量
     * @return 增加后的分数
     */
    public Double zIncrementScore(String key, String member, double delta) {
        return redisTemplate.opsForZSet().incrementScore(key, member, delta);
    }

    /**
     * 获取有序集合的Top N
     * 
     * @param key 键
     * @param start 起始位置
     * @param end 结束位置
     * @return 成员集合
     */
    public Set<Object> zReverseRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    /**
     * 获取有序集合成员的分数
     * 
     * @param key 键
     * @param member 成员
     * @return 分数
     */
    public Double zScore(String key, String member) {
        return redisTemplate.opsForZSet().score(key, member);
    }
}
