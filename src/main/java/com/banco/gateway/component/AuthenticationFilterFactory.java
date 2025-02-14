package com.banco.gateway.component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

import com.banco.gateway.service.ITokenCacheService;

@Slf4j
@Component
public class AuthenticationFilterFactory extends AbstractGatewayFilterFactory<AuthenticationFilterFactory.Config> {

    private final ITokenCacheService tokenCacheService;

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    public AuthenticationFilterFactory(ITokenCacheService tokenCacheService) {
        super(Config.class);
        this.tokenCacheService = tokenCacheService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                log.warn("Header Authorization no encontrado.");
                return unauthorizedResponse(exchange, "Falta el header Authorization", HttpStatus.UNAUTHORIZED);
            }

            String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (token == null || !token.startsWith("Bearer ")) {
                log.warn("Formato de token incorrecto. Debe ser 'Bearer <token>'. Token recibido: {}", token);
                return unauthorizedResponse(exchange, "Formato de token incorrecto. Debe ser 'Bearer <token>'", HttpStatus.BAD_REQUEST);
            }

            token = token.substring(7);

            return handleTokenValidation(exchange, chain, token);
        };
    }

    private Mono<Void> handleTokenValidation(ServerWebExchange exchange, GatewayFilterChain chain, String token) {
        if (!tokenCacheService.isTokenValid(token)) {
            log.info("Token no encontrado en cache. Validando con JWT...");
    
            try {
                if (validateJwtToken(token)) {
                    log.info("Token valido. Guardando en cache.");
                    tokenCacheService.addToken(token);
                } else {
                    log.info("Token invalido o expirado. No se agrega a la cache.");
                    return unauthorizedResponse(exchange, "Token JWT invalido o expirado", HttpStatus.UNAUTHORIZED);
                }
            } catch (JwtException | IllegalArgumentException e) {
                log.error("Error al validar el token: {}", e.getMessage());
                return unauthorizedResponse(exchange, "Token JWT inválido", HttpStatus.UNAUTHORIZED);
            }
        }
    
        log.info("Token valido. Continuando...");
        return chain.filter(exchange);
    }


    private boolean validateJwtToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return !claims.getExpiration().before(new Date()); 
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String errorMessage, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String errorBody = String.format("{\"error\": \"%s\"}", errorMessage);
        DataBuffer buffer = new DefaultDataBufferFactory().wrap(errorBody.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
    }
}

