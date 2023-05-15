

package com.yami.shop.common.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * redis 缓存配置，仅当配置文件中spring.cache.type = redis时生效
 * @author 北易航
 */
@EnableCaching
@Configuration
public class RedisCacheConfig  {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory, RedisSerializer<Object> redisSerializer) {
        // 创建缓存管理器
        RedisCacheManager redisCacheManager = new RedisCacheManager(
                RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory),
                // 默认策略，未配置的 key 会使用这个
                this.getRedisCacheConfigurationWithTtl(3600, redisSerializer),
                // 指定 key 策略
                this.getRedisCacheConfigurationMap(redisSerializer)
        );
        redisCacheManager.setTransactionAware(true);
        return redisCacheManager;
    }


    private Map<String, RedisCacheConfiguration> getRedisCacheConfigurationMap(RedisSerializer<Object> redisSerializer) {
        // 创建一个空的缓存配置映射
        Map<String, RedisCacheConfiguration> redisCacheConfigurationMap = new HashMap<>(16);

        // 添加缓存键为 "product" 的缓存配置
        // 使用 getRedisCacheConfigurationWithTtl 方法创建缓存配置，设置过期时间为 1800 秒（30 分钟）
        // 指定使用提供的 redisSerializer 进行对象的序列化和反序列化
        redisCacheConfigurationMap.put("product", this.getRedisCacheConfigurationWithTtl(1800, redisSerializer));

        // 返回创建的缓存配置映射
        return redisCacheConfigurationMap;
    }


    private RedisCacheConfiguration getRedisCacheConfigurationWithTtl(Integer seconds, RedisSerializer<Object> redisSerializer) {
        // 获取默认的缓存配置
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();

        // 设置缓存值的序列化方式
        redisCacheConfiguration = redisCacheConfiguration.serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer)
        );

        // 设置缓存条目的过期时间
        redisCacheConfiguration = redisCacheConfiguration.entryTtl(Duration.ofSeconds(seconds));

        // 返回设置了过期时间的缓存配置
        return redisCacheConfiguration;
    }


    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory, RedisSerializer<Object> redisSerializer) {
        // 创建 RedisTemplate 实例
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        // 设置 Redis 连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 设置键的序列化器为 StringRedisSerializer
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        // 设置哈希键的序列化器为 StringRedisSerializer
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // 设置值的序列化器为提供的 redisSerializer
        redisTemplate.setValueSerializer(redisSerializer);

        // 设置哈希值的序列化器为提供的 redisSerializer
        redisTemplate.setHashValueSerializer(redisSerializer);

        // 禁用事务支持
        redisTemplate.setEnableTransactionSupport(false);

        // 初始化 RedisTemplate
        redisTemplate.afterPropertiesSet();

        // 返回配置完成的 RedisTemplate 实例
        return redisTemplate;
    }


    /**
     * 自定义redis序列化的机制,重新定义一个ObjectMapper.防止和MVC的冲突
     * https://juejin.im/post/5e869d426fb9a03c6148c97e
     */
    @Bean
    public RedisSerializer<Object> redisSerializer() {
        ObjectMapper objectMapper = JsonMapper.builder().disable(MapperFeature.USE_ANNOTATIONS).build();
        // 反序列化时候遇到不匹配的属性并不抛出异常
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 序列化时候遇到空对象不抛出异常
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 反序列化的时候如果是无效子类型,不抛出异常
        objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        // 不使用默认的dateTime进行序列化,
        objectMapper.configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);
        // 使用JSR310提供的序列化类,里面包含了大量的JDK8时间序列化类
        objectMapper.registerModule(new JavaTimeModule());
        // 启用反序列化所需的类型信息,在属性中添加@class
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);
        // 配置null值的序列化器
        GenericJackson2JsonRedisSerializer.registerNullValueSerializer(objectMapper, null);
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }


    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        // 创建 StringRedisTemplate 实例
        StringRedisTemplate redisTemplate = new StringRedisTemplate(redisConnectionFactory);

        // 禁用事务支持
        redisTemplate.setEnableTransactionSupport(false);

        // 返回配置完成的 StringRedisTemplate 实例
        return redisTemplate;
    }


}
