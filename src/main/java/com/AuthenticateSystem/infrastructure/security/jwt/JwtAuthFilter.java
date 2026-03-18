package com.AuthenticateSystem.infrastructure.security.jwt;

import com.AuthenticateSystem.common.constants.RedisKeyConstants;
import com.AuthenticateSystem.common.constants.SecurityConstants;
import com.AuthenticateSystem.infrastructure.security.principal.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService               jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final StringRedisTemplate      redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest  request,
                                    HttpServletResponse response,
                                    FilterChain         chain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (StringUtils.hasText(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // 1. Validate signature + expiry
                if (jwtService.isTokenValid(token)) {

                    // 2. Check Redis blacklist — O(1), no DB query
                    String jti         = jwtService.extractJti(token);
                    String blacklistKey = RedisKeyConstants.blacklistToken(jti);
                    Boolean isRevoked  = redisTemplate.hasKey(blacklistKey);

                    if (Boolean.TRUE.equals(isRevoked)) {
                        log.warn("Revoked token used — jti: {}", jti);
                        // Do not set authentication — SecurityConfig returns 401
                    } else {
                        // 3. Load user and set SecurityContext
                        String      userId = jwtService.extractUserId(token);
                        UserDetails user   = userDetailsService
                                .loadUserById(UUID.fromString(userId));

                        var auth = new UsernamePasswordAuthenticationToken(
                                user, null, user.getAuthorities()
                        );
                        auth.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (Exception e) {
                // Any token error → clear context, let SecurityConfig handle 401
                SecurityContextHolder.clearContext();
                log.debug("JWT authentication failed: {}", e.getMessage());
            }
        }

        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(SecurityConstants.AUTH_HEADER);
        if (StringUtils.hasText(header)
                && header.startsWith(SecurityConstants.BEARER_PREFIX)) {
            return header.substring(SecurityConstants.BEARER_PREFIX.length());
        }
        return null;
    }
}