package com.security.gateway.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.security.gateway.service.ChallengeService;

@RestController
@RequestMapping("/api")
public class GatewayController {

    private final ChallengeService challengeService;

    public GatewayController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    @GetMapping("/challenge")
    public ResponseEntity<Map<String, String>> getChallenge(@RequestParam String clientId) {
        String nonce = challengeService.generateChallenge(clientId);
        return ResponseEntity.ok(Map.of(
            "nonce", nonce,
            "difficulty", "4",
            "algorithm", "SHA-256"
        ));
    }

    @GetMapping("/protected/resource")
    public ResponseEntity<Map<String, String>> getProtectedData() {
        return ResponseEntity.ok(Map.of("data", "Access Granted to Secure Infrastructure"));
    }
}