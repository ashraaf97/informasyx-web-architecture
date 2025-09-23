package com.example.demo.service.impl;

import com.example.demo.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenServiceImpl implements TokenService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.token.expiry.hours:24}")
    private int tokenExpiryHours;

    private static final String TOKEN_PREFIX = "auth_token:";

    @Override
    public void storeToken(String token, String username) {
        try {
            String key = TOKEN_PREFIX + token;
            redisTemplate.opsForValue().set(key, username, Duration.ofHours(tokenExpiryHours));
            log.debug("Token stored for user: {}", username);
        } catch (Exception e) {
            log.error("Failed to store token for user: {}", username, e);
        }
    }

    @Override
    public String getUsernameFromToken(String token) {
        try {
            String key = TOKEN_PREFIX + token;
            Object username = redisTemplate.opsForValue().get(key);
            return username != null ? username.toString() : null;
        } catch (Exception e) {
            log.error("Failed to retrieve username from token", e);
            return null;
        }
    }

    @Override
    public boolean isValidToken(String token) {
        try {
            String key = TOKEN_PREFIX + token;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Failed to validate token", e);
            return false;
        }
    }

    @Override
    public void removeToken(String token) {
        try {
            String key = TOKEN_PREFIX + token;
            redisTemplate.delete(key);
            log.debug("Token removed: {}", token);
        } catch (Exception e) {
            log.error("Failed to remove token", e);
        }
    }

    @Override
    public String generateToken(String username) {
        return "TOKEN_" + username + "_" + System.currentTimeMillis();
    }
}
