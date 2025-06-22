package com.example.rawsource.exceptions;

public class RateLimitExceededExceptione extends RuntimeException{
    
    private final long retryAfterSeconds;

    public RateLimitExceededExceptione(String message) {
        super(message);
        this.retryAfterSeconds = 60;
    }

    public RateLimitExceededExceptione(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
