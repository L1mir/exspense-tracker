package org.limir.expensetracker.limit;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import org.limir.expensetracker.common.ExpenseCategory;

/** New monthly limit. There is no date: the service always sets the current time. */
public record LimitRequest(
        @Schema(example = "product")
        @NotNull
        ExpenseCategory expenseCategory,

        @Schema(description = "Monthly limit in USD", example = "2000.00")
        @NotNull @Positive @Digits(integer = 17, fraction = 2)
        BigDecimal limitSum) {
}
