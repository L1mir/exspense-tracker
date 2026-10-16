package org.limir.expensetracker.rate.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Settings of the twelvedata.com client from the {@code app.twelve-data.*} section.
 *
 * @param baseUrl        API root, replaced by the WireMock URL in tests
 * @param apiKey         API key, comes from the TWELVEDATA_API_KEY environment variable
 * @param connectTimeout max time to open a connection
 * @param readTimeout    max time to wait for a response
 */
@Validated
@ConfigurationProperties("app.twelve-data")
public record TwelveDataProperties(
        @NotBlank String baseUrl,
        @NotBlank String apiKey,
        @NotNull Duration connectTimeout,
        @NotNull Duration readTimeout) {
}
