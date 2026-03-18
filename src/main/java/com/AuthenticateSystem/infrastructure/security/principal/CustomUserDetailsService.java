package com.AuthenticateSystem.infrastructure.security.principal;

import com.AuthenticateSystem.common.exceptions.AppException;
import com.AuthenticateSystem.common.exceptions.ErrorCode;
import com.AuthenticateSystem.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Used by Spring Security's DaoAuthenticationProvider
     * during form-based login (email + password).
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        return userRepository.findByEmailWithRolesAndPermissions(email)
                .map(UserPrincipal::from)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * Used by JwtAuthFilter to load the user by their UUID
     * extracted from the JWT subject claim.
     * Loads roles in the same query to avoid N+1.
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(UUID id) {
        return userRepository.findByIdWithRoles(id)
                .map(user -> {
                    UserPrincipal principal = UserPrincipal.from(user);
                    log.info("Loaded authorities for {}: {}", id, principal.getAuthorities());
                    return (UserDetails) principal;
                })
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}