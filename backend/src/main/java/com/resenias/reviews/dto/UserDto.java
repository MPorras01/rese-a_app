package com.resenias.reviews.dto;

import java.util.UUID;

import com.resenias.reviews.entity.User;

public record UserDto(
    UUID id,
    String email,
    String phone,
    boolean emailVerified,
    boolean phoneVerified,
    User.UserStatus status,
    String name,
    String avatarUrl,
    User.Role role
) {
}
