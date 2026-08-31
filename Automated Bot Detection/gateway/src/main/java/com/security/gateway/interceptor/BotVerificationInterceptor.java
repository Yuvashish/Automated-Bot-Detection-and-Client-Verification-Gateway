package com.security.gateway.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.security.gateway.service.AnomalyDetectionService;
import com.security.gateway.service.ChallengeService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class BotVerificationInterceptor implements HandlerInterceptor {

    private final ChallengeService challengeService;
    private final AnomalyDetectionService anomalyDetectionService;

    public BotVerificationInterceptor(ChallengeService challengeService, 
                                      AnomalyDetectionService anomalyDetectionService) {
        this.challengeService = challengeService;
        this.anomalyDetectionService = anomalyDetectionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        // 1. Sliding Window Rate Limiting (100 reqs per 60s)
        if (anomalyDetectionService.isRateLimited(clientIp, 100, 60)) {
            response.setStatus(429);
            response.getWriter().write("{\"error\": \"Rate limit exceeded. Try again later.\"}");
            return false;
        }

        // 2. Anomaly Detection based on Header Entropy
        double entropy = anomalyDetectionService.calculateHeaderEntropy(userAgent);
        if (entropy < 2.5) { // Suspicious low-variety bot headers
            response.setStatus(403);
            response.getWriter().write("{\"error\": \"Suspicious Request Signature\"}");
            return false;
        }

        // 3. PoW Challenge Verification on Protected Endpoints
        if (request.getRequestURI().startsWith("/api/protected")) {
            String nonce = request.getHeader("X-POW-Nonce");
            String solution = request.getHeader("X-POW-Solution");

            if (nonce == null || solution == null || !challengeService.verifyChallenge(nonce, solution)) {
                response.setStatus(401);
                response.getWriter().write("{\"error\": \"Challenge verification failed or missing.\"}");
                return false;
            }
        }

        return true;
    }
}