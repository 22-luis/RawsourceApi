package com.example.rawsource.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.bucket4j.*;

@Configuration
public class RateLimitConfig {
    
    @Value("${rate.limit.login.requests:1000}")
    private int loginRequests;
    
    @Value("${rate.limit.login.window.minutes:5}")
    private int loginWindowMinutes;
    
    @Value("${rate.limit.general.requests:10000}")
    private int generalRequests;
    
    @Value("${rate.limit.general.window.hours:1}")
    private int generalWindowHours;
    
    @Value("${rate.limit.critical.requests:5000}")
    private int criticalRequests;
    
    @Value("${rate.limit.critical.window.hours:1}")
    private int criticalWindowHours;
    
    @Bean
    @Qualifier("LoginBucket")
    public Bucket loginBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(loginRequests, Refill.intervally(loginRequests, Duration.ofMinutes(loginWindowMinutes))))
                .build();
    }

    @Bean
    @Qualifier("generalBucket")
    public Bucket generalBucket() {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(generalRequests, Refill.intervally(generalRequests, Duration.ofHours(generalWindowHours))))
            .build();
    }
    
    @Bean
    @Qualifier("criticalBucket")
    public Bucket criticalBucket() {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(criticalRequests, Refill.intervally(criticalRequests, Duration.ofHours(criticalWindowHours))))
            .build();
    }
}
