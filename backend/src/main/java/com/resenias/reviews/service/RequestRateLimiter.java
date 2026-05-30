package com.resenias.reviews.service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class RequestRateLimiter {

    private final ConcurrentHashMap<String, CounterWindow> windows = new ConcurrentHashMap<>();

    public boolean tryAcquire(String key, int maxAttempts, Duration windowDuration) {
        Instant now = Instant.now();
        CounterWindow window = windows.compute(key, (ignored, existing) -> {
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