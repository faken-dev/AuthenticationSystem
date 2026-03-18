package com.AuthenticateSystem.common.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // User
    USER_NOT_FOUND                (HttpStatus.NOT_FOUND, "User not found"),
    USER_ALREADY_EXISTS       (HttpStatus.CONFLICT, "Email already registerd"),

    // Auth
    INVALID_CREDENTIALS          (HttpStatus.UNAUTHORIZED, "Invalid email or password"),
    TOKEN_EXPIRED                       (HttpStatus.UNAUTHORIZED, "Token has expired"),
    TOKEN_INVALID                        (HttpStatus.UNAUTHORIZED, "Token is invalid"),
    TOKEN_REVOKED                     (HttpStatus.UNAUTHORIZED, "Token has been revoked"),
    UNAUTHORIZED                        (HttpStatus.UNAUTHORIZED, "Authentication required"),
    FORBIDDEN                                (HttpStatus.FORBIDDEN, "Access denied"),

    // OTP
    OTP_EXPIRED                              (HttpStatus.BAD_REQUEST, "OTP has expired"),
    OTP_INVALID                              (HttpStatus.BAD_REQUEST, "OTP code is incorrect"),
    OTP_MAX_ATTEMPTS                (HttpStatus.TOO_MANY_REQUESTS, "Too many failed OTP attempts"),
    OTP_COOLDOWN                         (HttpStatus.TOO_MANY_REQUESTS, "Please wait before requesting a new OTP"),

    // TOTP
    TOTP_NOT_ENABLED                  (HttpStatus.BAD_REQUEST, "Two factor authentication is not enabled"),
    TOTP_ALREADY_ENABLED         (HttpStatus.CONFLICT, "Two factor authentication is already enabled"),
    TOTP_INVALID_CODE                   (HttpStatus.BAD_REQUEST, "Invalid authenticator code"),

    //  Role
    ROLE_NOT_FOUND                       (HttpStatus.NOT_FOUND, "Role not found"),
    ROLE_ALREADY_ASSIGNED         (HttpStatus.CONFLICT, "User already has this role"),
    CANNOT_REMOVE_LAST_ROLE   (HttpStatus.BAD_REQUEST, "Cannot remove the last role from a user"),

    // File
    INVALID_FILE_TYPE                        (HttpStatus.BAD_REQUEST, "File must be JPG, PNG, WEBP"),
    FILE_TOO_LARGE                             (HttpStatus.BAD_REQUEST, "File size must not exceed 10 MB"),
    FILE_UPLOAD_FAILED                     (HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file"),

    // OAuth2
    PROVIDER_NOT_SUPPORTED          (HttpStatus.BAD_REQUEST, "OAuth2 provider is not supported"),

    // Rate limit
    RATE_LIMIT_EXCEEDED                    (HttpStatus.TOO_MANY_REQUESTS, "Too many requests, please try again later"),

    // Validation
    VALIDATION_ERROR                            (HttpStatus.UNPROCESSABLE_ENTITY, " Request validation failed"),

    // Generic
    INTERNAL_ERROR                                 (HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");

    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
