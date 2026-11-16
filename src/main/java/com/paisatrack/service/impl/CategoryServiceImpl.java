package com.paisatrack.service.impl;

import com.paisatrack.entity.Category;
import com.paisatrack.entity.CategoryType;
import com.paisatrack.repository.CategoryRepository;
import com.paisatrack.service.CategoryService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read-only service for loading INCOME and EXPENSE categories from MySQL.
 */
@Service
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> getCategoriesByType(CategoryType type) {
        return categoryRepository.findByCategoryTypeOrderByCategoryIdAsc(type);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByCategoryTypeAscCategoryIdAsc();
    }

    @Override
    public Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Selected category does not exist."));
    }
}
