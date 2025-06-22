package com.example.rawsource.services;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import io.github.bucket4j.*;

@Service
public class RateLimitService {
    
    private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> blacklistedIps = new ConcurrentHashMap<>();

    @Autowired
    @Qualifier("loginBucket")
    private Bucket loginBucket;

    @Autowired
    @Qualifier("generalBucket")
    private Bucket generalBucket;

    @Autowired
    @Qualifier("criticalBucket")
    private Bucket criticalBucket;

    public boolean isAllowed(String clientIp, String endpoint) {
        if (blacklistedIps.containsKey(clientIp)) {
            return false;
        }

        Bucket bucket = getOrCreateBucket(clientIp, endpoint);
            return bucket.tryConsume(1);
    }

    public void recordFailAttempt(String clientIp) {
        blacklistedIps.put(clientIp, LocalDateTime.now().plusMinutes(5));
    }

    public void recordSuccessAttempt(String clientIp) {
        blacklistedIps.remove(clientIp);
    }
    
        
    private Bucket getOrCreateBucket(String clientIp, String endpoint) {
        String key = clientIp + ":" + endpoint;
        return ipBuckets.computeIfAbsent(key, k -> createBucketForEndpoint(endpoint));
    }

    private Bucket createBucketForEndpoint(String endpoint) {
        if (endpoint.contains("/api/auth/login")) {
            return loginBucket;
        } else if (endpoint.contains("/api/users/register") || endpoint.contains("/api/products")) {
            return criticalBucket;
        } else {
            return generalBucket;
        }
    }

    public boolean isBlackListed(String clientIp) {
        LocalDateTime blackListTime = blacklistedIps.get(clientIp);
        if (blackListTime != null ) {
            if (LocalDateTime.now().isAfter(blackListTime.plusHours(1))) {
                blacklistedIps.remove(clientIp);
                return false;
            }
            return true;
        }
        return false;
    }

    @Scheduled(fixedDelay = 3600000)
    public void cleanupBlacklistedIps() {
        ipBuckets.clear();
    }
    
}
