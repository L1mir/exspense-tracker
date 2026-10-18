package org.limir.expensetracker.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app")
public record AppProperties(
        @NotNull ZoneId timeZone,
        @NotNull @Positive BigDecimal defaultLimit,
        @NotNull @Valid RatePrefetch ratePrefetch) {

    /**
     * @param currencies rates loaded in parallel every day, so transactions find them in the database
     * @param cron       when to load them, in {@code timeZone}
     */
    public record RatePrefetch(@NotNull List<String> currencies, @NotBlank String cron) {
    }
}
