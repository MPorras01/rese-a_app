package com.resenias.reviews.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.resenias.reviews.entity.User;
import com.resenias.reviews.repository.UserRepository;

@Configuration
public class DemoUserSeeder {

    @Bean
    public CommandLineRunner seedDemoUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            seedUser(
                userRepository,
                passwordEncoder,
                "admin@resenaapp.local",
                "Administrador Demo",
                "Admin123!",
                User.Role.ADMIN,
                "+5491100000001"
            );
            seedUser(
                userRepository,
                passwordEncoder,
                "owner@resenaapp.local",
                "Owner Demo",
                "Owner123!",
                User.Role.USER,
                "+5491100000002"
            );
            seedUser(
                userRepository,
                passwordEncoder,
                "user@resenaapp.local",
                "Usuario Demo",
                "User123!",
                User.Role.USER,
                "+5491100000003"
            );
        };
    }

    private void seedUser(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          String email,
                          String name,
                          String password,
                          User.Role role,
                          String phone) {
        userRepository.findByEmail(email).ifPresentOrElse(existingUser -> {
            existingUser.setName(name);
            existingUser.setRole(role);
            existingUser.setPhone(phone);
            existingUser.setEmailVerified(true);
            existingUser.setPhoneVerified(true);
            existingUser.setStatus(User.UserStatus.ACTIVE);
            existingUser.setOauthProvider(User.OAuthProvider.LOCAL);
            existingUser.setOauthSubject(null);
            existingUser.setPasswordHash(passwordEncoder.encode(password));
            userRepository.save(existingUser);
        }, () -> {
            User newUser = User.builder()
                .email(email)
                .name(name)
                .phone(phone)
                .emailVerified(true)
                .phoneVerified(true)
                .status(User.UserStatus.ACTIVE)
                .oauthProvider(User.OAuthProvider.LOCAL)
                .passwordHash(passwordEncoder.encode(password))
                .role(role)
                .build();
            userRepository.save(newUser);
        });
    }
}