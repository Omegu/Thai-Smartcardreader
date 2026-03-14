package com.idbridge.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple rate limiter to prevent API abuse
 * Uses token bucket algorithm with fixed capacity and refill rate
 */
@Service
public class RateLimitService {

    @Value("${ratelimit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${ratelimit.capacity:10}")
    private int capacity; // Number of requests allowed per refill period

    @Value("${ratelimit.refill-minutes:1}")
    private int refillMinutes; // Refill period in minutes

    // Store rate limit data per client key (IP)
    private final Map<String, ClientRateLimit> clientLimits = new ConcurrentHashMap<>();

    /**
     * Check if the request from a given key (e.g., IP) is allowed
     * @param key Client identifier (usually IP address)
     * @return true if allowed, false if rate limited
     */
    public boolean tryConsume(String key) {
        if (!rateLimitEnabled) {
            return true;
        }

        long now = System.currentTimeMillis();
        ClientRateLimit clientLimit = clientLimits.computeIfAbsent(key, k -> new ClientRateLimit(capacity));

        synchronized (clientLimit) {
            // Refill tokens based on time elapsed
            long timeElapsed = now - clientLimit.lastRefillTimestamp;
            long refillIntervalMs = refillMinutes * 60L * 1000L;
            if (timeElapsed >= refillIntervalMs) {
                long tokensToAdd = (timeElapsed / refillIntervalMs) * clientLimit.maxTokens;
                clientLimit.tokens = Math.min(clientLimit.maxTokens, clientLimit.tokens + tokensToAdd);
                clientLimit.lastRefillTimestamp = now;
            }

            // Check if we have tokens available
            if (clientLimit.tokens > 0) {
                clientLimit.tokens--;
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Get remaining tokens for a key
     */
    public long getRemainingTokens(String key) {
        if (!rateLimitEnabled) {
            return -1; // unlimited
        }
        ClientRateLimit clientLimit = clientLimits.get(key);
        if (clientLimit == null) {
            return capacity;
        }
        synchronized (clientLimit) {
            return clientLimit.tokens;
        }
    }

    /**
     * Reset rate limit for a key (for admin use)
     */
    public void reset(String key) {
        ClientRateLimit clientLimit = clientLimits.remove(key);
        if (clientLimit != null) {
            synchronized (clientLimit) {
                clientLimit.tokens = clientLimit.maxTokens;
            }
        }
    }

    /**
     * Inner class to store rate limit state per client
     */
    private static class ClientRateLimit {
        private final long maxTokens;
        private volatile long tokens;
        private volatile long lastRefillTimestamp;

        ClientRateLimit(long maxTokens) {
            this.maxTokens = maxTokens;
            this.tokens = maxTokens;
            this.lastRefillTimestamp = System.currentTimeMillis();
        }
    }
}
