package org.limir.expensetracker.transaction;

import java.math.BigDecimal;
import java.time.Instant;
import org.limir.expensetracker.common.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ExpenseTransactionRepository extends JpaRepository<ExpenseTransaction, Long> {

    /** Total USD spent by the account in the category within [from, to); 0 if there are no transactions. */
    @Query("""
            SELECT COALESCE(SUM(t.sumUsd), 0)
            FROM ExpenseTransaction t
            WHERE t.accountFrom.number = :accountNumber
              AND t.category = :category
              AND t.datetime >= :from
              AND t.datetime < :to
            """)
    BigDecimal sumUsdBetween(String accountNumber, ExpenseCategory category, Instant from, Instant to);
}
