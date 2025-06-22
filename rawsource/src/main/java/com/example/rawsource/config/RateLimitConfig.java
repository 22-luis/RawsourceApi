package com.example.rawsource.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.bucket4j.*;

@Configuration
public class RateLimitConfig {
    
    @Bean
    @Qualifier("LoginBucket")
    public Bucket loginBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(5))))
                .build();
    }

    @Bean
    @Qualifier("generalBucket")
    public Bucket generalBucket() {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(100, Refill.intervally(100, Duration.ofHours(1))))
            .build();
    }
    
    @Bean
    @Qualifier("criticalBucket")
    public Bucket criticalBucket() {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(50, Refill.intervally(50, Duration.ofHours(1))))
            .build();
    }
}
