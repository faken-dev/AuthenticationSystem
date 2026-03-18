package com.AuthenticateSystem.domain.user.repository;

import com.AuthenticateSystem.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * Find by OAuth2 provider + provider subject ID.
     * Used in OAuth2UserService to link returning social login users.
     */
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    /**
     * Find user with roles eagerly loaded.
     * Avoids LazyInitializationException outside of a transaction.
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") UUID id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    @Query("""
    SELECT DISTINCT u FROM User u
    LEFT JOIN FETCH u.roles r
    LEFT JOIN FETCH r.permissions
    WHERE u.email = :email
""")
    Optional<User> findByEmailWithRolesAndPermissions(String email);

    @Query("""
    SELECT u FROM User u
    LEFT JOIN FETCH u.roles r
    LEFT JOIN FETCH r.permissions
    WHERE u.id = :id
""")
    Optional<User> findByIdWithRolesAndPermissions(@Param("id") UUID id);
}