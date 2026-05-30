package com.resenias.reviews.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LocalLoginDto(
    @NotBlank
    @Email
    String email,

    @NotBlank
    String password
) {
}
