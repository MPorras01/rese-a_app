package com.resenias.reviews.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.resenias.reviews.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    Optional<User> findByOauthProviderAndOauthSubject(String oauthProvider, String oauthSubject);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}
