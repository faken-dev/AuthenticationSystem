package com.AuthenticateSystem.domain.role.entity;

import com.AuthenticateSystem.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "permissions")
public class Permission extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

}