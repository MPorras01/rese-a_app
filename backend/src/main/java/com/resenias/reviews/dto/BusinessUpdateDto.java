package com.resenias.reviews.dto;

public record BusinessUpdateDto(
    String name,
    String description,
    String category,
    String address,
    String city,
    String phone,
    String email,
    String website
) {
}
