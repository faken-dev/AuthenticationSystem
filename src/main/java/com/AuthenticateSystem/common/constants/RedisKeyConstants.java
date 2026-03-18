package com.AuthenticateSystem.common.constants;

public final class RedisKeyConstants {

    private RedisKeyConstants() {}

    // OTP storage — value is SHA-256(code)
    public static final String OTP_EMAIL     = "otp:email:%s";
    public static final String OTP_WHATSAPP  = "otp:whatsapp:%s";

    // Failed attempt counter per target
    public static final String OTP_ATTEMPTS  = "otp:attempts:%s";

    // Resend cooldown — key exists = still in cooldown
    public static final String OTP_COOLDOWN  = "otp:cooldown:%s:%s";

    // JWT blacklist — key exists = token is revoked
    public static final String BLACKLIST_TOKEN = "blacklist:token:%s";

    // Rate limit buckets (managed by Bucket4j)
    public static final String RATE_LIMIT    = "rate:limit:%s:%s";

    // ── Formatters ────────────────────────────────────────────────────────

    public static String otpEmail(String email) {
        return OTP_EMAIL.formatted(email);
    }

    public static String otpWhatsapp(String phone) {
        return OTP_WHATSAPP.formatted(phone);
    }

    public static String otpAttempts(String target) {
        return OTP_ATTEMPTS.formatted(target);
    }

    public static String otpCooldown(String target, String channel) {
        return OTP_COOLDOWN.formatted(target, channel);
    }

    public static String blacklistToken(String jti) {
        return BLACKLIST_TOKEN.formatted(jti);
    }

    public static String rateLimit(String action, String identifier) {
        return RATE_LIMIT.formatted(action, identifier);
    }
}