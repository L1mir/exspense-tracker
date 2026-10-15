package org.limir.expensetracker.config;

import jakarta.validation.constraints.NotNull;
import java.time.ZoneId;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Application settings from the {@code app.*} section of application.yaml.
 *
 * @param timeZone time zone that defines month boundaries and the "day" of a transaction
 */
@Validated
@ConfigurationProperties("app")
public record AppProperties(@NotNull ZoneId timeZone) {
}
