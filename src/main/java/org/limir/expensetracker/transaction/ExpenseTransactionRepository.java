package org.limir.expensetracker.transaction;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseTransactionRepository extends JpaRepository<ExpenseTransaction, Long> {
}
