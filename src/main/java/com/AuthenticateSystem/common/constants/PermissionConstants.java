package com.AuthenticateSystem.common.constants;

public final class PermissionConstants {

    private PermissionConstants() {}

    public static final String USER_READ         = "USER_READ";
    public static final String USER_WRITE        = "USER_WRITE";
    public static final String USER_DELETE       = "USER_DELETE";

    public static final String ROLE_READ         = "ROLE_READ";
    public static final String ROLE_ASSIGN       = "ROLE_ASSIGN";
    public static final String ROLE_REVOKE       = "ROLE_REVOKE";

    public static final String PERMISSION_READ   = "PERMISSION_READ";
    public static final String PERMISSION_ASSIGN = "PERMISSION_ASSIGN";
    public static final String PERMISSION_REVOKE = "PERMISSION_REVOKE";

    public static final String PROFILE_READ      = "PROFILE_READ";
    public static final String PROFILE_WRITE     = "PROFILE_WRITE";
    public static final String AVATAR_UPLOAD     = "AVATAR_UPLOAD";

    public static final String OTP_SEND          = "OTP_SEND";
    public static final String OTP_VERIFY        = "OTP_VERIFY";
    public static final String TOTP_MANAGE       = "TOTP_MANAGE";
}
