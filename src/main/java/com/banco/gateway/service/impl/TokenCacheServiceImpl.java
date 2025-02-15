package com.banco.gateway.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.banco.gateway.record.TokenStatus;
import com.banco.gateway.service.ITokenCacheService;

@Service
public class TokenCacheServiceImpl implements ITokenCacheService{

    private final Cache<String, TokenStatus> tokenCache;

    public TokenCacheServiceImpl() {
        this.tokenCache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build();
    }

    @Override
    public void addToken(String token) {
        TokenStatus status = new TokenStatus(false, false);
        tokenCache.put(token, status);
    }

    @Override
    public boolean isTokenValid(String token) {
        TokenStatus status = tokenCache.getIfPresent(token);
        return status != null && !status.isExpired() && !status.isRevoked();
    }

    @Override
    public void removeToken(String token) {
        tokenCache.invalidate(token);
    }

    @Override
    public void markTokenAsRevokedAndExpired(String token) {
        TokenStatus status = new TokenStatus(true, true);
        tokenCache.put(token, status);
    }

    @Override
    public TokenStatus getTokenStatus(String token) {
        return tokenCache.getIfPresent(token);
    }

}
