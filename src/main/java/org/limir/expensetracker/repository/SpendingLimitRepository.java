package org.limir.expensetracker.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.limir.expensetracker.entity.SpendingLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SpendingLimitRepository extends JpaRepository<SpendingLimit, Long> {

    // Active limit at the moment: the latest one set at or before it (subquery with MAX + JOIN)
    @Query(value = """
            SELECT l.*
            FROM spending_limit l
            JOIN (SELECT MAX(set_at) AS max_set_at
                  FROM spending_limit
                  WHERE account_number = :accountNumber
                    AND category = :category
                    AND set_at <= :moment) latest ON l.set_at = latest.max_set_at
            WHERE l.account_number = :accountNumber
              AND l.category = :category
            ORDER BY l.id DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<SpendingLimit> findActiveLimit(String accountNumber, String category, Instant moment);

    List<SpendingLimit> findAllByAccountNumberOrderBySetAtDesc(String accountNumber);
}
