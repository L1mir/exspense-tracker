package org.limir.expensetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.limir.expensetracker.entity.ExpenseCategory;
import org.limir.expensetracker.util.ValidationPatterns;

public record TransactionRequest(
        @Schema(description = "Client account, 10 digits", example = "0000000123")
        @NotNull @Pattern(regexp = ValidationPatterns.ACCOUNT_NUMBER, message = "must be 10 digits")
        String accountFrom,

        @Schema(description = "Counterparty account, 10 digits", example = "9999999999")
        @NotNull @Pattern(regexp = ValidationPatterns.ACCOUNT_NUMBER, message = "must be 10 digits")
        String accountTo,

        @Schema(description = "ISO 4217 currency code", example = "KZT")
        @NotNull @Pattern(regexp = ValidationPatterns.CURRENCY_CODE, message = "must be an ISO 4217 code, e.g. KZT")
        String currencyShortname,

        @Schema(description = "Amount with up to 2 decimal places", example = "10000.45")
        @NotNull @Positive @Digits(integer = 17, fraction = 2)
        BigDecimal sum,

        @Schema(example = "product")
        @NotNull
        ExpenseCategory expenseCategory,

        @Schema(description = "ISO 8601 timestamp with time zone", example = "2022-01-30T00:00:00+06:00")
        @NotNull
        OffsetDateTime datetime) {
}
