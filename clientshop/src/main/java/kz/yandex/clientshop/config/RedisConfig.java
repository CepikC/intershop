package kz.yandex.clientshop.config;

import kz.yandex.clientshop.model.Item;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.*;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, Item> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
        // 🔹 сериализатор ключей (строки)
        RedisSerializer<String> keySerializer = new StringRedisSerializer();

        // 🔹 сериализатор значений (JSON через Jackson)
        Jackson2JsonRedisSerializer<Item> valueSerializer =
                new Jackson2JsonRedisSerializer<>(Item.class);

        RedisSerializationContext<String, Item> context = RedisSerializationContext
                .<String, Item>newSerializationContext(keySerializer)
                .value(valueSerializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}