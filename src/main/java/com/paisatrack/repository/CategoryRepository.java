package com.paisatrack.repository;

import com.paisatrack.entity.Category;
import com.paisatrack.entity.CategoryType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for shared INCOME and EXPENSE categories.
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByCategoryTypeOrderByCategoryIdAsc(CategoryType categoryType);

    List<Category> findAllByOrderByCategoryTypeAscCategoryIdAsc();
}
