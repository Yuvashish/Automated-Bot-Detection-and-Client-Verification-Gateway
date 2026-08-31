package com.security.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.security.gateway.interceptor.BotVerificationInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final BotVerificationInterceptor botInterceptor;

    public WebConfig(BotVerificationInterceptor botInterceptor) {
        this.botInterceptor = botInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(botInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/challenge"); // Exclude challenge endpoint so clients can request nonces freely
    }
}