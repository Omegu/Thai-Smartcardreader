package com.idbridge.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Statistics service to track card reading metrics
 */
@Service
public class StatsService {

    private final AtomicLong totalReads = new AtomicLong(0);
    private final AtomicLong successfulReads = new AtomicLong(0);
    private final AtomicLong failedReads = new AtomicLong(0);
    private final AtomicLong lastReadTimestamp = new AtomicLong(0);
    private final Map<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();

    public void incrementTotal() {
        totalReads.incrementAndGet();
        lastReadTimestamp.set(System.currentTimeMillis());
    }

    public void incrementSuccess() {
        successfulReads.incrementAndGet();
    }

    public void incrementFailure(String errorType) {
        failedReads.incrementAndGet();
        errorCounts.computeIfAbsent(errorType, k -> new AtomicLong(0)).incrementAndGet();
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("totalReads", totalReads.get());
        stats.put("successfulReads", successfulReads.get());
        stats.put("failedReads", failedReads.get());
        stats.put("successRate", calculateSuccessRate());
        stats.put("lastReadTimestamp", lastReadTimestamp.get());
        stats.put("errorCounts", new ConcurrentHashMap<>(errorCounts));
        return stats;
    }

    private double calculateSuccessRate() {
        long total = totalReads.get();
        if (total == 0) return 0.0;
        return (double) successfulReads.get() / total * 100;
    }

    public void reset() {
        totalReads.set(0);
        successfulReads.set(0);
        failedReads.set(0);
        lastReadTimestamp.set(0);
        errorCounts.clear();
    }
}
