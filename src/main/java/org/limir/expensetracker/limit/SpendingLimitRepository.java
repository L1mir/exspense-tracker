package org.limir.expensetracker.limit;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SpendingLimitRepository extends JpaRepository<SpendingLimit, Long> {

    /**
     * The limit active at the given moment: the latest limit of the account and category set at or before it.
     * The subquery finds the latest set_at with MAX, the JOIN returns the limit row with that set_at.
     */
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
}
