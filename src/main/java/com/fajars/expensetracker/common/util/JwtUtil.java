package com.fajars.expensetracker.common.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long EXPIRATION_TIME = 86400000; // 1 day

    public JwtUtil(@Value("${jwt.secret:default_jwt_secret_change_me}") String secret) {
        byte[] keyBytes;
        try {
            // try treat as base64-encoded key
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (Exception ex) {
            // fallback: use raw bytes of the provided secret
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        // ensure key is at least 32 bytes for HS256
        if (keyBytes.length < 32) {
            keyBytes = Arrays.copyOf(keyBytes, 32);
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UUID userId, String email) {
        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .signWith(key)
            .compact();
    }

    public UUID extractUserId(String token) {
        Claims claims = getClaims(token);
        return claims != null
            ? UUID.fromString(claims.getSubject())
            : null;
    }

    public String extractEmail(String token) {
        Claims claims = getClaims(token);
        return claims != null
            ? claims.get("email", String.class)
            : null;
    }

    /* =====================
       VALIDATION
       ===================== */

    public Claims validateAndGetClaims(String token) {
        Claims claims = getClaims(token);
        if (claims == null || claims.getExpiration().before(new Date())) {
            throw new JwtException("Invalid or expired token");
        }
        return claims;
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = getClaims(token);
            return claims != null && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        try {
            return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }
}
