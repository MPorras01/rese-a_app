package com.resenias.reviews.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.resenias.reviews.dto.UpdateProfileDto;
import com.resenias.reviews.dto.UserDto;
import com.resenias.reviews.entity.User;
import com.resenias.reviews.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User markPhoneVerified(UUID userId, String phone) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPhone(phone);
        user.setPhoneVerified(true);

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            user.setStatus(User.UserStatus.ACTIVE);
        }

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserDto getById(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return toDto(user);
    }

    @Transactional
    public UserDto updateProfile(UUID userId, UpdateProfileDto dto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(dto.name());
        user.setAvatarUrl(dto.avatarUrl());

        return toDto(userRepository.save(user));
    }

    private UserDto toDto(User user) {
        return new UserDto(
            user.getId(),
            user.getEmail(),
            user.getPhone(),
            Boolean.TRUE.equals(user.getEmailVerified()),
            Boolean.TRUE.equals(user.getPhoneVerified()),
            user.getStatus(),
            user.getName(),
            user.getAvatarUrl(),
            user.getRole()
        );
    }
}
