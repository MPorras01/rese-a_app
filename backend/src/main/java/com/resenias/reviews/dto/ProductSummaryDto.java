package com.resenias.reviews.dto;

import java.util.UUID;

public record ProductSummaryDto(
    UUID id,
    String name,
    String description,
    String priceRange,
    boolean active
) {
}
