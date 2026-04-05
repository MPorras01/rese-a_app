package com.resenias.reviews.service;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Service
public class OtpBlacklistService {

    private final Cache<String, Boolean> usedJtiCache;

    public OtpBlacklistService() {
        this.usedJtiCache = Caffeine.newBuilder()
            .expireAfterWrite(6, TimeUnit.MINUTES)
            .maximumSize(50_000)
            .build();
    }

    public boolean isUsed(String jti) {
        return usedJtiCache.getIfPresent(jti) != null;
    }

    public void markUsed(String jti) {
        usedJtiCache.put(jti, Boolean.TRUE);
    }
}
