package com.security.gateway.service;

import java.util.Map;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

@Service
public class AnomalyDetectionService {

    private final StringRedisTemplate redisTemplate;

    public AnomalyDetectionService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isRateLimited(String clientIp, int maxRequests, long windowInSeconds) {
        String key = "ratelimit:" + clientIp;
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - (windowInSeconds * 1000);

        ZSetOperations<String, String> zset = redisTemplate.opsForZSet();

        // Remove outdated logs outside current window
        zset.removeRangeByScore(key, 0, windowStart);

        Long currentCount = zset.zCard(key);
        if (currentCount != null && currentCount >= maxRequests) {
            return true;
        }

        zset.add(key, String.valueOf(currentTime), currentTime);
        return false;
    }

    public double calculateHeaderEntropy(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) return 0.0;
        Map<Character, Integer> frequencyMap = new java.util.HashMap<>();
        for (char c : userAgent.toCharArray()) {
            frequencyMap.put(c, frequencyMap.getOrDefault(c, 0) + 1);
        }

        double entropy = 0.0;
        int length = userAgent.length();
        for (Integer freq : frequencyMap.values()) {
            double prob = (double) freq / length;
            entropy -= prob * (Math.log(prob) / Math.log(2));
        }
        return entropy;
    }
}