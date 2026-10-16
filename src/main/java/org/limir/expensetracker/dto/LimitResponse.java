package org.limir.expensetracker.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.limir.expensetracker.entity.ExpenseCategory;

public record LimitResponse(
        ExpenseCategory expenseCategory,
        BigDecimal limitSum,
        OffsetDateTime limitDatetime,
        String limitCurrencyShortname) {
}
