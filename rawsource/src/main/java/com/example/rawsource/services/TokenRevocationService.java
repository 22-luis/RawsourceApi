package com.example.rawsource.services;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class TokenRevocationService {
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    private final Set<String> revokedTokens = ConcurrentHashMap.newKeySet();
    private final Map<String, LocalDateTime> tokenExpirationTimes = new ConcurrentHashMap<>();
    
    @Scheduled(fixedRate = 3600000) // Cada hora
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        tokenExpirationTimes.entrySet().removeIf(entry -> 
            entry.getValue().isBefore(now));
        
        // Limpiar tokens revocados expirados
        revokedTokens.removeIf(token -> 
            !tokenExpirationTimes.containsKey(token));
    }
    
    public void revokeToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String actualToken = token.substring(7);
            revokedTokens.add(actualToken);
            
            // Obtener tiempo de expiración del token
            try {
                Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(actualToken)
                    .getBody();
                
                Date expiration = claims.getExpiration();
                if (expiration != null) {
                    tokenExpirationTimes.put(actualToken, 
                        expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                }
            } catch (Exception e) {
                // Token inválido, no hacer nada
            }
        }
    }
    
    public boolean isTokenRevoked(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String actualToken = token.substring(7);
            return revokedTokens.contains(actualToken);
        }
        return false;
    }
    
    private Key getSigningKey() {
        // Usar la misma clave que en JwtService
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
