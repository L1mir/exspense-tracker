package org.limir.expensetracker.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.ZoneId;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Application settings from the {@code app.*} section of application.yaml.
 *
 * @param timeZone     time zone that defines month boundaries and the "day" of a transaction
 * @param defaultLimit monthly limit in USD used when the client has not set one
 */
@Validated
@ConfigurationProperties("app")
public record AppProperties(@NotNull ZoneId timeZone, @NotNull @Positive BigDecimal defaultLimit) {
}
