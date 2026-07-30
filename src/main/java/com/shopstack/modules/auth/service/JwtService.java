package com.shopstack.modules.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    // Secret and expiration are externalized to config (application*.properties / env vars)
    // instead of being hardcoded, so every environment (and every developer) doesn't share the
    // same signing key. Set JWT_SECRET_KEY as an environment variable in production.
    @Value("${application.security.jwt.secret-key}")
    private String secret;

    @Value("${application.security.jwt.expiration:86400000}")
    private long expirationMs;

    private SecretKey key;

    @PostConstruct
    void init() {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            // HMAC-SHA256 requires at least a 256-bit (32-byte) key. Fail fast at startup
            // rather than silently signing tokens with a weak key.
            throw new IllegalStateException(
                    "application.security.jwt.secret-key must be at least 32 characters (256 bits) long");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
    }

    public String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    public String extractEmail(String token){
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token){
        return extractAllClaims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token){
        try{extractAllClaims(token);
            return true;
        } catch(Exception e){
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}