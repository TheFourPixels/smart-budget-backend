package com.teamfourpixels.repository;

import com.teamfourpixels.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserIdOrIsSystem(Long userId, boolean isSystem);
}