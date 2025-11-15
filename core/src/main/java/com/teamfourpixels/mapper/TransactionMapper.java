package com.teamfourpixels.mapper;

import com.teamfourpixels.dto.*;
import com.teamfourpixels.entity.Transaction;
import com.teamfourpixels.service.CategoryService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class TransactionMapper {

    @Autowired
    protected CategoryService categoryService;

    @Mapping(target = "amount", expression = "java(entity.getType() == com.teamfourpixels.enums.OperationType.EXPENSE ? entity.getAmount().negate() : entity.getAmount())")
    @Mapping(target = "external_id", source = "bankTransactionRefId")
    @Mapping(target = "transaction_date", source = "transactionTime")
    @Mapping(target = "merchant_name", source = "merchant")
    @Mapping(target = "parent_transaction_id", source = "originalTransactionId")
    @Mapping(target = "category", source = "categoryId")
    public abstract TransactionDto toDto(Transaction entity);

    public abstract List<TransactionDto> toDtoList(List<Transaction> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "isSplit", constant = "false")
    @Mapping(target = "originalTransactionId", ignore = true)
    @Mapping(target = "bankTransactionRefId", constant = "null")
    @Mapping(target = "categoryId", source = "request.categoryId", defaultValue = "999L")
    public abstract Transaction toEntity(CreateTransactionRequest request, Long userId);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "bankTransactionRefId", ignore = true)
    @Mapping(target = "originalTransactionId", ignore = true)
    @Mapping(target = "isSplit", ignore = true)
    @Mapping(target = "type", ignore = true)
    public abstract void updateEntity(CreateTransactionRequest request, @MappingTarget Transaction entity);

    protected CategoryDto mapCategory(Long categoryId) {
        return categoryId == null ? null : categoryService.getCategoryById(categoryId);
    }
}