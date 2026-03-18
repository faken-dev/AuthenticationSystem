package com.AuthenticateSystem.infrastructure.security.jwt;

import com.AuthenticateSystem.common.exceptions.AppException;
import com.AuthenticateSystem.common.exceptions.ErrorCode;
import com.AuthenticateSystem.config.properties.JwtProperties;
import com.AuthenticateSystem.infrastructure.security.principal.UserPrincipal;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    // Signing key
    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    // Generate
    public String generateAccessToken(UserPrincipal principal) {
        return buildToken(principal, jwtProperties.getAccessTokenExpiry());
    }

    public String generateRefreshToken(UserPrincipal principal) {
        return buildToken(principal, jwtProperties.getRefreshTokenExpiry());
    }

    private String buildToken(UserPrincipal principal, long expirySeconds) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(principal.getId().toString())
                .claim("email", principal.getEmail())
                .claim("roles", principal.getAuthorities()
                        .stream()
                        .map(a -> a.getAuthority())
                        .toList())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirySeconds)))
                .signWith(signingKey())
                .compact();
    }

    // Parse
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUserId(String token) {
        return parseToken(token).getSubject();
    }

    public String extractJti(String token) {
        return parseToken(token).getId();
    }

    public long extractRemainingSeconds(String token) {
        Date expiration = parseToken(token).getExpiration();
        long remaining  = expiration.getTime() - System.currentTimeMillis();
        return Math.max(0, remaining / 1000);
    }

    // Validate
    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException e) {
            log.warn("JWT invalid: {}", e.getMessage());
            throw new AppException(ErrorCode.TOKEN_INVALID);
        }
    }
}