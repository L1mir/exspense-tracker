package org.limir.expensetracker.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.limir.expensetracker.entity.ExpenseCategory;

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
