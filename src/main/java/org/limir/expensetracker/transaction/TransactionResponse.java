package org.limir.expensetracker.transaction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.limir.expensetracker.common.ExpenseCategory;

/** Saved transaction with its USD amount and the limit check result. */
public record TransactionResponse(
        Long id,
        String accountFrom,
        String accountTo,
        String currencyShortname,
        BigDecimal sum,
        ExpenseCategory expenseCategory,
        OffsetDateTime datetime,
        BigDecimal sumUsd,
        boolean limitExceeded) {
}
