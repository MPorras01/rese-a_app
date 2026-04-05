package com.resenias.reviews.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileDto(
    @NotBlank
    @Size(max = 120)
    String name,

    @Size(max = 500)
    String avatarUrl
) {
}
