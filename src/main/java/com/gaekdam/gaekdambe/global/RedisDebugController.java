package com.gaekdam.gaekdambe.global;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/__debug")
@RequiredArgsConstructor
@Slf4j
public class RedisDebugController {

    private final RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/redis")
    public String redisTest() {
        try {
            log.info(">>> Redis debug start");

            redisTemplate.opsForValue().set("debug:test", "ok");
            Object v = redisTemplate.opsForValue().get("debug:test");

            log.info(">>> Redis debug success, value={}", v);
            return "REDIS OK: " + v;
        } catch (Exception e) {
            log.error(">>> Redis debug FAIL", e);
            return "REDIS FAIL: " + e.getClass().getName() + " / " + e.getMessage();
        }
    }
}

