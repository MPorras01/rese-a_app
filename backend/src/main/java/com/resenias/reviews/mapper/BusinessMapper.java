package com.resenias.reviews.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.resenias.reviews.dto.BusinessDto;
import com.resenias.reviews.entity.Business;

@Mapper(componentModel = "spring")
public interface BusinessMapper {

    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "ownerName", source = "owner.name")
    @Mapping(target = "avgRating", ignore = true)
    BusinessDto toDto(Business business);
}
