package org.hongxi.whatsmars.ai.cache;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * AI 缓存配置
 * <p>
 * 使用 Redis 作为缓存后端，为 AI 响应提供缓存支持
 * </p>
 *
 * @author hongxi
 */
@Configuration
@EnableCaching
public class AiCacheConfig implements CachingConfigurer {

    /**
     * 配置 Redis CacheManager
     * <p>
     * 设置不同的缓存策略：
     * - ai-chat: 聊天响应缓存，TTL 30分钟
     * - ai-embedding: 向量嵌入缓存，TTL 1小时
     * - ai-search: 搜索结果缓存，TTL 2小时
     * - ai-rag: RAG 文档缓存，TTL 6小时
     * </p>
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 配置 JSON 序列化器
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        
        GenericJackson2JsonRedisSerializer jsonSerializer = 
                new GenericJackson2JsonRedisSerializer(objectMapper);

        // 默认缓存配置（30分钟过期）
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        // 针对不同场景的缓存配置
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        
        // 聊天响应缓存 - 30分钟
        cacheConfigs.put("ai-chat", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // 向量嵌入缓存 - 1小时（向量计算成本高）
        cacheConfigs.put("ai-embedding", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // 搜索结果缓存 - 2小时（相对稳定）
        cacheConfigs.put("ai-search", defaultConfig.entryTtl(Duration.ofHours(2)));
        
        // RAG 文档缓存 - 6小时
        cacheConfigs.put("ai-rag", defaultConfig.entryTtl(Duration.ofHours(6)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}
