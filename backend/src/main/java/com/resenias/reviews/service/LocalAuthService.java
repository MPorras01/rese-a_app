package com.resenias.reviews.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.resenias.reviews.entity.User;
import com.resenias.reviews.repository.UserRepository;
import com.resenias.reviews.security.JwtService;

@Service
public class LocalAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LocalAuthService(UserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public String login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
            .orElseThrow(() -> new UnauthorizedException("Credenciales invalidas"));

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new UnauthorizedException("La cuenta no tiene acceso local habilitado");
        }

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new UnauthorizedException("Credenciales invalidas");
        }

        if (user.getStatus() == User.UserStatus.SUSPENDED) {
            throw new UnauthorizedException("La cuenta esta suspendida");
        }

        return jwtService.generateToken(user);
    }
}