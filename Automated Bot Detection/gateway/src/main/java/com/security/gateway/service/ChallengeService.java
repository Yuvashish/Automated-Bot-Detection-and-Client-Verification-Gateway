package com.security.gateway.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChallengeService {

    private final StringRedisTemplate redisTemplate;
    private static final int DIFFICULTY = 4; // Target: 4 leading hexadecimal zeros
    private static final long TTL_SECONDS = 60;

    public ChallengeService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String generateChallenge(String clientId) {
        String nonce = UUID.randomUUID().toString();
        String redisKey = "pow:nonce:" + nonce;
        
        // Store nonce in Redis to prevent replay attacks (Single Use)
        redisTemplate.opsForValue().set(redisKey, clientId, Duration.ofSeconds(TTL_SECONDS));
        return nonce;
    }

    public boolean verifyChallenge(String nonce, String solution) {
        String redisKey = "pow:nonce:" + nonce;
        String storedClient = redisTemplate.opsForValue().get(redisKey);

        if (storedClient == null) {
            return false; // Expired or already used
        }

        // Atomically consume nonce
        redisTemplate.delete(redisKey);

        String input = nonce + solution;
        String hash = computeSha256(input);
        
        String targetPrefix = "0".repeat(DIFFICULTY);
        return hash.startsWith(targetPrefix);
    }

    private String computeSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm missing", e);
        }
    }
}