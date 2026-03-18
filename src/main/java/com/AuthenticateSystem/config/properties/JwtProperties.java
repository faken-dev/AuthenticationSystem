package com.AuthenticateSystem.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /** HMAC-SHA256 signing secret*/
    private String secret;

    /** Access token lifetime in seconds (default 15 min) */
    private long accessTokenExpiry = 900;

    /** Refresh token lifetime in seconds (default 30 days) */
    private long refreshTokenExpiry = 2592000;

    /** JWT issuer claim */
    private String issuer = "AuthenticateSystem.com";
}