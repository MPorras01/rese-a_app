package com.resenias.reviews.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OtpVerifyDto(
    @NotBlank
    String otpToken,

    @NotBlank
    @Size(min = 6, max = 6, message = "code must have 6 characters")
    String code
) {
}
