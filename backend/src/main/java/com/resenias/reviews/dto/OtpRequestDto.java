package com.resenias.reviews.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OtpRequestDto(
    @NotBlank
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "phone must be in E.164 format")
    String phone
) {
}
