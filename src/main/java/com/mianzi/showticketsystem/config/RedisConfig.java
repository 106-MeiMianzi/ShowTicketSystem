package com.mianzi.showticketsystem.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类
 * 
 * 作用：
 * - 配置Redis连接和序列化方式
 * - 提供RedisTemplate Bean供其他组件使用
 * 
 * 说明：
 * - Redis 的 Value 使用独立的 ObjectMapper（带类型信息，仅用于 Redis 序列化）
 * - 不覆盖 Spring MVC 的默认 ObjectMapper，保证 API 返回标准 JSON 对象 {}，而非 [类名, {}]
 */
@Configuration
public class RedisConfig {

    /**
     * 配置RedisTemplate
     * 
     * 仅在此处创建「带类型信息」的 ObjectMapper，用于 Redis 序列化，
     * 不暴露为全局 Bean，避免影响 HTTP 接口的 JSON 返回格式。
     * 
     * @param connectionFactory Redis连接工厂
     * @return 配置好的RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key序列化器：使用String序列化
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Value序列化器：仅用于 Redis，使用带类型信息的 ObjectMapper（不暴露为全局 Bean）
        ObjectMapper redisObjectMapper = new ObjectMapper();
        redisObjectMapper.registerModule(new JavaTimeModule()); // 支持 LocalDateTime 等 Java 8 时间类型
        redisObjectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        redisObjectMapper.activateDefaultTyping(
                redisObjectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        jsonSerializer.setObjectMapper(redisObjectMapper);

        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
