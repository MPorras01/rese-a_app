package com.resenias.reviews.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BusinessUpdateDto(
    @NotBlank(message = "name es requerido")
    @Size(max = 200, message = "name máximo 200 caracteres")
    String name,

    @Size(max = 2000, message = "description máximo 2000 caracteres")
    String description,

    @NotBlank(message = "category es requerida")
    String category,

    @Size(max = 300, message = "address máximo 300 caracteres")
    String address,

    @Size(max = 100, message = "city máximo 100 caracteres")
    String city,

    @Size(max = 30, message = "phone máximo 30 caracteres")
    String phone,

    @Email(message = "email inválido")
    @Size(max = 200, message = "email máximo 200 caracteres")
    String email,

    @Size(max = 500, message = "website máximo 500 caracteres")
    String website
) {
}
