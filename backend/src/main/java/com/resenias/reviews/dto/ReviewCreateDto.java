package com.resenias.reviews.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewCreateDto(
    @NotNull(message = "rating es requerido")
    @Min(value = 1, message = "rating mínimo es 1")
    @Max(value = 5, message = "rating máximo es 5")
    Integer rating,

    @NotBlank(message = "body es requerido")
    String body,

    List<String> photos,

    UUID productId
) {
}
