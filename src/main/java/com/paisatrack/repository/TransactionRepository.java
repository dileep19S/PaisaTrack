package com.paisatrack.repository;

import com.paisatrack.entity.CategoryType;
import com.paisatrack.entity.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for user-isolated Transaction queries and MySQL aggregations.
 * All queries filter strictly by userId to enforce per-user data isolation.
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("""
        SELECT t FROM Transaction t
        JOIN FETCH t.category
        WHERE t.transactionId = :transactionId
          AND t.user.userId = :userId
        """)
    Optional<Transaction> findByIdAndUserId(
        @Param("transactionId") Long transactionId,
        @Param("userId") Long userId
    );

    @Query("""
        SELECT t FROM Transaction t
        JOIN FETCH t.category
        WHERE t.user.userId = :userId
        ORDER BY t.transactionDate DESC, t.transactionId DESC
        """)
    List<Transaction> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("""
        SELECT t FROM Transaction t
        JOIN FETCH t.category
        WHERE t.user.userId = :userId
          AND (:type IS NULL OR t.transactionType = :type)
          AND (:startDate IS NULL OR t.transactionDate >= :startDate)
          AND (:endDate IS NULL OR t.transactionDate <= :endDate)
        ORDER BY t.transactionDate DESC, t.transactionId DESC
        """)
    List<Transaction> findFilteredByUser(
        @Param("userId") Long userId,
        @Param("type") CategoryType type,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("""
        SELECT SUM(t.amount)
        FROM Transaction t
        WHERE t.user.userId = :userId
          AND t.transactionType = :type
        """)
    BigDecimal sumAmountByUserAndType(
        @Param("userId") Long userId,
        @Param("type") CategoryType type
    );

    @Query("""
        SELECT SUM(t.amount)
        FROM Transaction t
        WHERE t.user.userId = :userId
          AND t.transactionType = :type
          AND t.transactionDate BETWEEN :startDate AND :endDate
        """)
    BigDecimal sumAmountByUserAndTypeBetween(
        @Param("userId") Long userId,
        @Param("type") CategoryType type,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("""
        SELECT c.categoryName, SUM(t.amount)
        FROM Transaction t
        JOIN t.category c
        WHERE t.user.userId = :userId
          AND t.transactionType = :type
          AND (:startDate IS NULL OR t.transactionDate >= :startDate)
          AND (:endDate IS NULL OR t.transactionDate <= :endDate)
        GROUP BY c.categoryId, c.categoryName
        ORDER BY SUM(t.amount) DESC
        """)
    List<Object[]> sumByCategoryForUser(
        @Param("userId") Long userId,
        @Param("type") CategoryType type,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("""
        SELECT MONTH(t.transactionDate), t.transactionType, SUM(t.amount)
        FROM Transaction t
        WHERE t.user.userId = :userId
          AND YEAR(t.transactionDate) = :year
        GROUP BY MONTH(t.transactionDate), t.transactionType
        ORDER BY MONTH(t.transactionDate) ASC
        """)
    List<Object[]> findMonthlyTotalsByYear(
        @Param("userId") Long userId,
        @Param("year") int year
    );

    @Query("""
        SELECT DISTINCT YEAR(t.transactionDate)
        FROM Transaction t
        WHERE t.user.userId = :userId
        ORDER BY YEAR(t.transactionDate) DESC
        """)
    List<Integer> findDistinctYearsByUserId(@Param("userId") Long userId);
}
