package com.paisatrack.service;

import com.paisatrack.entity.Category;
import com.paisatrack.entity.CategoryType;
import java.util.List;

/**
 * Service interface for retrieving shared INCOME and EXPENSE categories.
 */
public interface CategoryService {

    List<Category> getCategoriesByType(CategoryType type);

    List<Category> getAllCategories();

    Category getCategoryById(Long categoryId);
}
