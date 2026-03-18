package com.AuthenticateSystem.infrastructure.security.principal;

import com.AuthenticateSystem.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class UserPrincipal implements UserDetails, OAuth2User {

    private final UUID   id;
    private final String email;
    private final String password;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    // OAuth2 attributes — null when using form/JWT login
    private Map<String, Object> attributes;

    private UserPrincipal(UUID id,
                          String email,
                          String password,
                          boolean enabled,
                          Collection<? extends GrantedAuthority> authorities,
                          Map<String, Object> attributes) {
        this.id          = id;
        this.email       = email;
        this.password    = password;
        this.enabled     = enabled;
        this.authorities = authorities;
        this.attributes = attributes;
    }

    // Factory: JWT filter / form login
    public static UserPrincipal from(User user) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        user.getRoles().forEach(role -> {
            // Add ROLE_ prefixed authority — used with hasRole()
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

            // Add each permission — used with hasAuthority()
            role.getPermissions().forEach(permission ->
                    authorities.add(new SimpleGrantedAuthority(permission.getName()))
            );
        });

        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                authorities.stream().distinct().toList(),
                null
        );
    }

    // Factory: OAuth2 flow
    public static UserPrincipal fromOAuth2(User user, Map<String, Object> attributes) {
        UserPrincipal principal = from(user);
        principal.attributes   = attributes;
        return principal;
    }

    // UserDetails
    @Override public String   getUsername()              { return email; }
    @Override public boolean  isAccountNonExpired()      { return true; }
    @Override public boolean  isAccountNonLocked()       { return true; }
    @Override public boolean  isCredentialsNonExpired()  { return true; }

    // OAuth2User
    @Override public String               getName()       { return id.toString(); }
    @Override public Map<String, Object>  getAttributes() { return attributes; }
}