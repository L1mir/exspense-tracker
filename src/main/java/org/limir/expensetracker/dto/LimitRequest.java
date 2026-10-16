package org.limir.expensetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import org.limir.expensetracker.entity.ExpenseCategory;

public record LimitRequest(
        @Schema(example = "product")
        @NotNull
        ExpenseCategory expenseCategory,

        @Schema(description = "Monthly limit in USD", example = "2000.00")
        @NotNull @Positive @Digits(integer = 17, fraction = 2)
        BigDecimal limitSum) {
}
