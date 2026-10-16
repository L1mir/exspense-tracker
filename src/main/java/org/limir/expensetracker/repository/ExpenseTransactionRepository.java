package org.limir.expensetracker.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.limir.expensetracker.entity.ExpenseCategory;
import org.limir.expensetracker.entity.ExpenseTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ExpenseTransactionRepository extends JpaRepository<ExpenseTransaction, Long> {

    @Query("""
            SELECT COALESCE(SUM(t.sumUsd), 0)
            FROM ExpenseTransaction t
            WHERE t.accountFrom.number = :accountNumber
              AND t.category = :category
              AND t.datetime >= :from
              AND t.datetime < :to
            """)
    BigDecimal sumUsdBetween(String accountNumber, ExpenseCategory category, Instant from, Instant to);

    @Query("""
            SELECT t
            FROM ExpenseTransaction t
            JOIN FETCH t.spendingLimit
            WHERE t.accountFrom.number = :accountNumber
              AND t.limitExceeded = true
            ORDER BY t.datetime, t.id
            """)
    List<ExpenseTransaction> findExceeded(String accountNumber);
}
