package com.cusc.neutrino.application;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Slf4j
public class RedisConnectionTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void testRedisSetAndGet() {
        log.info("开始测试Redis连接...");
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String testKey = "neutrino:test:" + UUID.randomUUID();
        String testValue = "connection_successful";

        log.info("向Redis写入键值对: {} = {}", testKey, testValue);
        ops.set(testKey, testValue);

        String retrievedValue = ops.get(testKey);
        log.info("从Redis读取值: {}", retrievedValue);

        assertEquals(testValue, retrievedValue, "从Redis读取的值与写入的值不匹配");
        log.info("Redis连接和读写测试成功！");
        
        // 清理测试数据
        stringRedisTemplate.delete(testKey);
    }
}