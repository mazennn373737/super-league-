package com.superleague.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisCacheService {

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    private boolean isEnabled() {
        return redisTemplate != null;
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        if (isEnabled()) {
            try {
                redisTemplate.opsForValue().set(key, value, timeout, unit);
            } catch (Exception e) {
                System.err.println("Redis set error: " + e.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        if (isEnabled()) {
            try {
                return (T) redisTemplate.opsForValue().get(key);
            } catch (Exception e) {
                System.err.println("Redis get error: " + e.getMessage());
            }
        }
        return null;
    }
}
