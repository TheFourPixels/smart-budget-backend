package com.teamfourpixels.mapper;

import com.teamfourpixels.dto.CategoryDto;
import com.teamfourpixels.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDto toDto(Category entity);
}
