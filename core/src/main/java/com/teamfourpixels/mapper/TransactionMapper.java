package com.teamfourpixels.mapper;

import com.teamfourpixels.dto.*;
import com.teamfourpixels.entity.Transaction;
import org.mapstruct.*;
import com.teamfourpixels.enums.OperationType;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public abstract class TransactionMapper {

    @Mapping(target = "amount", source = "entity")
    @Mapping(target = "externalId", source = "bankTransactionRefId")
    @Mapping(target = "transactionDate", source = "transactionTime")
    @Mapping(target = "merchantName", source = "merchant")
    @Mapping(target = "category", source = "categoryId", qualifiedByName = "mapCategory")
    public abstract TransactionDto toDto(Transaction entity);

    protected BigDecimal mapAmount(Transaction entity) {
        if (entity.getType() == OperationType.EXPENSE) {
            return entity.getAmount().negate();
        }
        return entity.getAmount();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bankTransactionRefId", constant = "null")
    @Mapping(target = "categoryId", source = "request.categoryId", defaultValue = "999L")
    public abstract Transaction toEntity(CreateTransactionRequest request, Long userId);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "bankTransactionRefId", ignore = true)
    @Mapping(target = "type", ignore = true)
    public abstract void updateEntity(CreateTransactionRequest request, @MappingTarget Transaction entity);

    @Named("mapCategory")
    protected CategoryDto mapCategory(Long categoryId) {
        return categoryId == null ? null : CategoryDto.builder()
                .id(categoryId)
                .name("Неизвестная категория")
                .isSystem(true)
                .build();
    }
}