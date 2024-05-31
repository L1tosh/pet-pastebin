package org.example.pastebin.services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class RedisService<K, V> {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final Class<V> valueType;

    public void save(K key, V value) {
        executeSafely(() -> {
            String jsonKey = toJson(key);
            String jsonValue = toJson(value);
            redisTemplate.opsForValue().set(jsonKey, jsonValue);
            return null;
        });
    }

    public void saveWithTTL(K key, V value, long ttlInSeconds) {
        executeSafely(() -> {
            save(key, value);
            setTTL(key, ttlInSeconds);
            return null;
        });
    }

    public void setTTL(K key, long ttlInSeconds) {
        executeSafely(() -> {
            String jsonKey = toJson(key);
            redisTemplate.expire(jsonKey, ttlInSeconds, TimeUnit.SECONDS);
            return null;
        });
    }

    public V get(K key) {
        return executeSafely(() -> {
            String jsonKey = toJson(key);
            String jsonValue = redisTemplate.opsForValue().get(jsonKey);
            return toObject(jsonValue, valueType);
        });
    }

    public void delete(K key) {
        executeSafely(() -> {
            String jsonKey = toJson(key);
            redisTemplate.delete(jsonKey);
            return null;
        });
    }

    private String toJson(Object value) throws JsonProcessingException {
        return objectMapper.writeValueAsString(value);
    }

    private V toObject(String json, Class<V> type) throws JsonProcessingException {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return objectMapper.readValue(json, type);
    }

    private <T> T executeSafely(Callable<T> callable) {
        try {
            return callable.call();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON processing error", e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error", e);
        }
    }
}

