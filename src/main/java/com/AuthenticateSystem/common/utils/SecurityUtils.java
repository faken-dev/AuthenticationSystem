package com.AuthenticateSystem.common.utils;

import com.AuthenticateSystem.common.exceptions.AppException;
import com.AuthenticateSystem.common.exceptions.ErrorCode;
import com.AuthenticateSystem.infrastructure.security.principal.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * Returns the authenticated UserPrincipal from the current SecurityContext.
     * Throws UNAUTHORIZED if no authentication is present.
     */
    public static UserPrincipal getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return (UserPrincipal) auth.getPrincipal();
    }

    /**
     * Returns the UUID of the currently authenticated user.
     */
    public static UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }
}