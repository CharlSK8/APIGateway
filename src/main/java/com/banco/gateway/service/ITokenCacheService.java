package com.banco.gateway.service;

public interface ITokenCacheService {

    public void addToken(String token);
    public boolean isTokenValid(String token);
    public void removeToken(String token);

}