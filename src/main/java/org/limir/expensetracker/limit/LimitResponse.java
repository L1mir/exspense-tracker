package org.limir.expensetracker.limit;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.limir.expensetracker.common.ExpenseCategory;

public record LimitResponse(
        ExpenseCategory expenseCategory,
        BigDecimal limitSum,
        OffsetDateTime limitDatetime,
        String limitCurrencyShortname) {
}
