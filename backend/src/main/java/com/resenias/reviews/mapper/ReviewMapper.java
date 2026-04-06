package com.resenias.reviews.mapper;

import java.util.Arrays;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.resenias.reviews.dto.ReviewDto;
import com.resenias.reviews.entity.Review;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "userId",      source = "user.id")
    @Mapping(target = "userName",    source = "user.name")
    @Mapping(target = "userAvatar",  source = "user.avatarUrl")
    @Mapping(target = "businessId",  source = "business.id")
    @Mapping(target = "productId",   source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "rating",      source = "rating", qualifiedByName = "shortToInteger")
    @Mapping(target = "photos",      source = "photos", qualifiedByName = "arrayToList")
    ReviewDto toDto(Review review);

    @Named("shortToInteger")
    static Integer shortToInteger(Short value) {
        return value == null ? null : value.intValue();
    }

    @Named("arrayToList")
    static List<String> arrayToList(String[] photos) {
        return photos == null ? List.of() : Arrays.asList(photos);
    }
}
