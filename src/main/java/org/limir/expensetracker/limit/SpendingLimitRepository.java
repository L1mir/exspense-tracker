package org.limir.expensetracker.limit;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpendingLimitRepository extends JpaRepository<SpendingLimit, Long> {
}
