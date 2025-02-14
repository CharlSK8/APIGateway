package com.banco.gateway.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.banco.gateway.service.ITokenCacheService;

@Service
public class TokenCacheServiceImpl implements ITokenCacheService{

    private final Cache<String, Boolean> tokenCache;

    public TokenCacheServiceImpl() {
        this.tokenCache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build();
    }

    @Override
    public void addToken(String token) {
        tokenCache.put(token, true);
        
    }

    @Override
    public boolean isTokenValid(String token) {
        return tokenCache.getIfPresent(token) != null;
    }

    @Override
    public void removeToken(String token) {
        tokenCache.invalidate(token);
    }

}
