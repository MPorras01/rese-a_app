package com.resenias.reviews.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.resenias.reviews.entity.Business;
import com.resenias.reviews.entity.Review;
import com.resenias.reviews.entity.User;
import com.resenias.reviews.repository.BusinessRepository;
import com.resenias.reviews.repository.ReviewRepository;
import com.resenias.reviews.repository.UserRepository;

@Configuration
@Profile({ "dev", "docker" })
public class DevSampleDataConfig {

    @Bean
    public ApplicationRunner seedSampleData(UserRepository userRepository,
                                            BusinessRepository businessRepository,
                                            ReviewRepository reviewRepository,
                                            PasswordEncoder passwordEncoder) {
        return args -> {
            if (businessRepository.countByStatus(Business.BusinessStatus.APPROVED) > 0) {
                return;
            }

            User owner = userRepository.findByEmail("dueno.demo@resena.local")
                .orElseGet(() -> userRepository.save(User.builder()
                    .email("dueno.demo@resena.local")
                    .emailVerified(true)
                    .phoneVerified(true)
                    .status(User.UserStatus.ACTIVE)
                    .oauthProvider(User.OAuthProvider.LOCAL)
                    .name("Dueno Demo")
                    .passwordHash(passwordEncoder.encode("Owner12345!"))
                    .role(User.Role.USER)
                    .build()));

            User reviewer = userRepository.findByEmail("cliente.demo@resena.local")
                .orElseGet(() -> userRepository.save(User.builder()
                    .email("cliente.demo@resena.local")
                    .emailVerified(true)
                    .phoneVerified(true)
                    .status(User.UserStatus.ACTIVE)
                    .oauthProvider(User.OAuthProvider.LOCAL)
                    .name("Cliente Demo")
                    .passwordHash(passwordEncoder.encode("Client12345!"))
                    .role(User.Role.USER)
                    .build()));

            Business business = businessRepository.save(Business.builder()
                .owner(owner)
                .name("Cafe Demo ReseñaApp")
                .description("Cafe de prueba para validar el flujo completo del frontend y backend.")
                .category("Restaurante")
                .address("Calle 123 #45-67")
                .city("Bogota")
                .phone("+573001112233")
                .email("cafe.demo@resena.local")
                .website("https://example.com/demo")
                .status(Business.BusinessStatus.APPROVED)
                .build());

            reviewRepository.save(Review.builder()
                .user(reviewer)
                .business(business)
                .rating((short) 5)
                .body("Muy buena experiencia. El producto llego rapido y la atencion fue excelente.")
                .build());
        };
    }
}
