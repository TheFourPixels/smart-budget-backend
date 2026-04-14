package com.teamfourpixels.service;

import com.teamfourpixels.dto.AnalyticsEventDto;
import com.teamfourpixels.dto.CategoryDto;
import com.teamfourpixels.dto.CreateCategoryRequest;
import com.teamfourpixels.entity.Category;
import com.teamfourpixels.mapper.CategoryMapper;
import com.teamfourpixels.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    private final KafkaTemplate<String, Object> analyticsKafkaTemplate;
    private static final String ANALYTICS_TOPIC = "analytics-events";

    @Override
    @Transactional
    public CategoryDto createCategory(Long userId, CreateCategoryRequest request) {

        if (categoryRepository.existsByNameForUserOrIsSystem(userId, request.getName())) {
            throw new IllegalArgumentException(
                    String.format("Ошибка: Категория с именем '%s' уже существует.", request.getName()));
        }

        Category category = Category.builder()
                .userId(userId)
                .name(request.getName())
                .isSystem(false)
                .build();

        Category savedCategory = categoryRepository.save(category);

        sendAnalyticsEvent(userId, "CATEGORY_CREATED", "{\"categoryName\":\"" + savedCategory.getName() + "\"}");

        return categoryMapper.toDto(savedCategory);
    }

    @Override
    public Page<CategoryDto> getAllCategories(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return categoryRepository.findByUserIdOrIsSystem(userId, true, pageable)
                .map(categoryMapper::toDto);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long userId, Long id, CreateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Категория не найдена"));

        if (!userId.equals(category.getUserId()) || category.isSystem()) {
            throw new IllegalArgumentException("Нельзя редактировать системную или чужую категорию");
        }

        category.setName(request.getName());
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long userId, Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Категория не найдена"));

        if (!userId.equals(category.getUserId()) || category.isSystem()) {
            throw new IllegalArgumentException("Нельзя удалять системные или чужие категории");
        }
        categoryRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .map(categoryMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Категория не найдена с таким id: " + categoryId));
    }

    private void sendAnalyticsEvent(Long userId, String eventType, String payload) {
        try {
            AnalyticsEventDto event = AnalyticsEventDto.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(eventType)
                    .userId(userId)
                    .timestamp(LocalDateTime.now())
                    .platform("UNKNOWN")
                    .payload(payload)
                    .build();

            analyticsKafkaTemplate.send(ANALYTICS_TOPIC, userId.toString(), event);
            log.info("Аналитическое событие {} успешно отправлено для пользователя {}", eventType, userId);
        } catch (Exception e) {
            log.error("Ошибка при отправке аналитики в Kafka: {}", e.getMessage());
        }
    }
}