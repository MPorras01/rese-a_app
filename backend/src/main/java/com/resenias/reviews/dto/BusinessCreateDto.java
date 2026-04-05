package com.resenias.reviews.dto;

public record BusinessCreateDto(
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
