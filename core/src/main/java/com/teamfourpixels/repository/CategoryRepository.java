package com.teamfourpixels.repository;

import com.teamfourpixels.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Page<Category> findByUserIdOrIsSystem(Long userId, boolean isSystem, Pageable pageable);
    Page<Category> findByUserId(Long userId, Pageable pageable);
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE " +
            "(c.userId = :userId AND c.name = :name) OR " +
            "(c.isSystem = TRUE AND c.name = :name)")
    boolean existsByNameForUserOrIsSystem(
            @Param("userId") Long userId,
            @Param("name") String name);
    List<Category> findByIdIn(List<Long> ids);
}