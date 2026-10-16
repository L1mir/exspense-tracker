package org.limir.expensetracker.transaction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.limir.expensetracker.common.ExpenseCategory;

/** Incoming expense transaction from a bank system. */
public record TransactionRequest(
        String accountFrom,
        String accountTo,
        String currencyShortname,
        BigDecimal sum,
        ExpenseCategory expenseCategory,
        OffsetDateTime datetime) {
}
