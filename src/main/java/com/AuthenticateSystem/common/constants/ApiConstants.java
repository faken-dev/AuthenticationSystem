package com.AuthenticateSystem.common.constants;

public final class ApiConstants {

    private ApiConstants() {}

    public static final String API_V1          = "/api/v1";
    public static final String AUTH_PATH       = API_V1 + "/auth";
    public static final String USER_PATH       = API_V1 + "/user";
    public static final String ROLE_PATH       = API_V1 + "/roles";
    public static final String OTP_PATH        = API_V1 + "/otp";
    public static final String TOTP_PATH       = API_V1 + "/totp";
    public static final String OAUTH2_CALLBACK = "/oauth2/callback";
}