package com.resenias.reviews.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.resenias.reviews.entity.User;
import com.resenias.reviews.repository.UserRepository;

@Configuration
@Profile({ "dev", "docker" })
public class DevAdminSeedConfig {

    @Bean
    public ApplicationRunner seedAdminUser(UserRepository userRepository,
                                           PasswordEncoder passwordEncoder,
                                           @Value("${app.seed-admin.enabled:true}") boolean enabled,
                                           @Value("${app.seed-admin.email:admin@resena.local}") String email,
                                           @Value("${app.seed-admin.password:Admin12345!}") String password,
                                           @Value("${app.seed-admin.name:Administrador Demo}") String name) {
        return args -> {
            if (!enabled || userRepository.existsByEmail(email)) {
                return;
            }

            User user = User.builder()
                .email(email)
                .emailVerified(true)
                .phoneVerified(true)
                .status(User.UserStatus.ACTIVE)
                .oauthProvider(User.OAuthProvider.LOCAL)
                .name(name)
                .passwordHash(passwordEncoder.encode(password))
                .role(User.Role.ADMIN)
                .build();

            userRepository.save(user);
        };
    }
}
