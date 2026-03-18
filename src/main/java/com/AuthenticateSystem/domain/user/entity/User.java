package com.AuthenticateSystem.domain.user.entity;

import com.AuthenticateSystem.common.entity.BaseEntity;
import com.AuthenticateSystem.domain.role.entity.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 255)
    private  String email;

    /**
     * Password null for oauth2 user
     * BCrypt hash
     */
    @Column(length = 255)
    private String password;

    @Column(nullable = false, length = 100)
    private  String fullName;

    @Column(columnDefinition = "TEXT")
    private  String avatarUrl;

    /**
     *  Identity provider Local |Google | Facebook
     *  Local = register with email + password
     */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String provider = "LOCAL";

    /**
     * Subject ID from the OAuth2 provider (e.g. Google sub claim).
     * Null for LOCAL users.
     */
    @Column(length = 255)
    private String providerId;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean totpEnabled = false;

    /**
     * Many-to-many with Role.
     * FetchType.EAGER — roles are always needed for security decisions.
     * CascadeType omitted intentionally — role lifecycle is managed independently.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns        = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();


    // Helpers
    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    public boolean hasRole(String roleName) {
        return this.roles.stream()
                .anyMatch(r -> r.getName().equals(roleName));
    }
}
