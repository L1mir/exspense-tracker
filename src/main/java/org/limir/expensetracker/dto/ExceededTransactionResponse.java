package org.limir.expensetracker.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.limir.expensetracker.entity.ExpenseCategory;

public record ExceededTransactionResponse(
        String accountFrom,
        String accountTo,
        String currencyShortname,
        BigDecimal sum,
        ExpenseCategory expenseCategory,
        OffsetDateTime datetime,
        BigDecimal limitSum,
        OffsetDateTime limitDatetime,
        String limitCurrencyShortname) {
}
