package org.limir.expensetracker.transaction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.limir.expensetracker.common.ExpenseCategory;

/** Transaction that exceeded the monthly limit, together with the exceeded limit. */
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
