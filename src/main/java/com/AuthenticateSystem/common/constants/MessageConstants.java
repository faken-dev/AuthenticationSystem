package com.AuthenticateSystem.common.constants;

public final class MessageConstants {

    private MessageConstants() {}

    // Auth
    public static final String REGISTER_SUCCESS    = "Registration successful";
    public static final String LOGIN_SUCCESS       = "Login successful";
    public static final String LOGOUT_SUCCESS      = "Logged out successfully";
    public static final String TOKEN_REFRESHED     = "Token refreshed";
    public static final String TOTP_REQUIRED       = "TOTP verification required";

    // OTP
    public static final String OTP_SENT           = "OTP sent successfully";
    public static final String OTP_VERIFIED       = "OTP verified successfully";

    // TOTP
    public static final String TOTP_SETUP_SUCCESS = "Scan the QR code in Google Authenticator";
    public static final String TOTP_ENABLED       = "Two-factor authentication enabled";
    public static final String TOTP_DISABLED      = "Two-factor authentication disabled";

    // User
    public static final String PROFILE_UPDATED    = "Profile updated successfully";
    public static final String AVATAR_UPDATED     = "Avatar updated successfully";

    // Role
    public static final String ROLE_ASSIGNED      = "Role assigned successfully";
    public static final String ROLE_REVOKED       = "Role revoked successfully";
}