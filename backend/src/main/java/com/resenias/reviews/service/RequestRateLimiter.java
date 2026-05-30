package com.resenias.reviews.service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Component
public class RequestRateLimiter {

    // Max 10 minutes TTL — covers the longest window used (10 min for login).
    // Entries are automatically evicted after expiry, preventing unbounded growth.
    private final Cache<String, CounterWindow> windows = Caffeine.newBuilder()
        .expireAfterWrite(15, TimeUnit.MINUTES)
        .maximumSize(10_000)
        .build();

    public boolean tryAcquire(String key, int maxAttempts, Duration windowDuration) {
        Instant now = Instant.now();
        CounterWindow window = windows.asMap().compute(key, (ignored, existing) -> {
            if (existing == null || now.isAfter(existing.windowEndsAt())) {
                return new CounterWindow(1, now.plus(windowDuration));
            }
            return new CounterWindow(existing.count() + 1, existing.windowEndsAt());
        });

        return window != null && window.count() <= maxAttempts;
    }

    private record CounterWindow(int count, Instant windowEndsAt) {
    }
}