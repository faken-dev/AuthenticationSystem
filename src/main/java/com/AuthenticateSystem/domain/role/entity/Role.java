package com.AuthenticateSystem.domain.role.entity;

import com.AuthenticateSystem.common.entity.BaseEntity;
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
@Table(name = "roles")
public class Role extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /**
     * Permissions assigned to this role.
     * EAGER — always needed when building GrantedAuthority list in UserPrincipal.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns        = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    // Helpers
    public void addPermission(Permission permission) {
        this.permissions.add(permission);
    }

    public void removePermission(Permission permission) {
        this.permissions.remove(permission);
    }

    public boolean hasPermission(String permissionName) {
        return this.permissions.stream()
                .anyMatch(p -> p.getName().equals(permissionName));
    }

}