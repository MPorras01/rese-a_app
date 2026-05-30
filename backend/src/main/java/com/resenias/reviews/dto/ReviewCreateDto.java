package com.resenias.reviews.dto;

import java.util.UUID;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewCreateDto(
    @NotNull(message = "businessId es requerido")
    UUID businessId,

    UUID productId,

    @NotNull(message = "rating es requerido")
    @Min(value = 1, message = "rating mínimo es 1")
    @Max(value = 5, message = "rating máximo es 5")
    Short rating,

    @NotBlank(message = "body es requerido")
    @Size(min = 10, max = 2000, message = "body debe tener entre 10 y 2000 caracteres")
    String body,

    @Size(max = 4, message = "Se permiten hasta 4 fotos por reseña")
    List<@Size(max = 350000, message = "Cada foto es demasiado grande") String> photos
) {
}
