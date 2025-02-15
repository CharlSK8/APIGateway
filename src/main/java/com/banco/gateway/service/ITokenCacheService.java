package com.banco.gateway.service;

import com.banco.gateway.record.TokenStatus;

public interface ITokenCacheService {

    void addToken(String token);
    boolean isTokenValid(String token);
    void removeToken(String token);
    void markTokenAsRevokedAndExpired(String token);
    TokenStatus getTokenStatus(String token);

}