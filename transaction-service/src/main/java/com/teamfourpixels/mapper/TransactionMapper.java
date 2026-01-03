package com.teamfourpixels.mapper;

import com.teamfourpixels.dto.*;
import com.teamfourpixels.entity.Transaction;
import com.teamfourpixels.entity.TransactionSplit;
import com.teamfourpixels.repository.TransactionSplitRepository;
import com.teamfourpixels.service.CategoryQueryService;
import jakarta.persistence.EntityNotFoundException;
import org.mapstruct.*;
import com.teamfourpixels.enums.OperationType;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class TransactionMapper {

    @Autowired
    private CategoryQueryService categoryQueryService;

    @Autowired
    private TransactionSplitRepository splitRepository;

    private static final Long UNCATEGORIZED_ID = 999L;
    private static final Long SPLIT_CATEGORY_ID = -1L;

    @Mapping(target = "amount", expression = "java(entity.getAmount().abs())")
    @Mapping(target = "externalId", source = "bankTransactionRefId")
    @Mapping(target = "transactionDate", source = "transactionTime")
    @Mapping(target = "merchantName", source = "merchant")
    @Mapping(target = "category", source = "categoryId", qualifiedByName = "mapCategory")
    @Mapping(target = "isIncome", expression = "java(entity.getType() == com.teamfourpixels.enums.OperationType.INCOME)")
    @Mapping(target = "splits", expression = "java(mapSplits(entity))")
    public abstract TransactionDto toDto(Transaction entity);

    protected List<SplitPartDto> mapSplits(Transaction entity) {
        if (SPLIT_CATEGORY_ID.equals(entity.getCategoryId())) {
            List<TransactionSplit> splits = splitRepository.findByTransactionId(entity.getId());
            if (splits != null) {
                return splits.stream()
                        .map(s -> {
                            SplitPartDto dto = new SplitPartDto();
                            dto.setCategoryId(s.getCategoryId());
                            dto.setAmount(s.getAmount());
                            dto.setDescription(s.getDescription());
                            return dto;
                        })
                        .toList();
            }
        }
        return Collections.emptyList();
    }

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
        if (categoryId == null || categoryId.equals(UNCATEGORIZED_ID)) {
            return CategoryDto.builder().id(UNCATEGORIZED_ID).name("Не распределено").isSystem(true).build();
        }
        if (categoryId.equals(SPLIT_CATEGORY_ID)) {
            return CategoryDto.builder().id(SPLIT_CATEGORY_ID).name("Разделенная транзакция").isSystem(true).build();
        }

        try {
            return categoryQueryService.getCategoryById(categoryId);
        } catch (EntityNotFoundException e) {
            return CategoryDto.builder().id(categoryId).name("Категория не найдена").isSystem(true).build();
        }
    }
}