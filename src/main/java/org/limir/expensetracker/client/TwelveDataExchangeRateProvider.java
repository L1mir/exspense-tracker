package org.limir.expensetracker.client;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.limir.expensetracker.exception.ExchangeRateUnavailableException;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

/**
 * twelvedata has no KZT/USD pair, only USD/KZT, so USD/{currency} is requested and inverted:
 * 1 / 452.7 KZT = 0.0022 USD.
 */
@Component
@RequiredArgsConstructor
public class TwelveDataExchangeRateProvider implements ExchangeRateProvider {

    private static final int RATE_SCALE = 10;

    private final TwelveDataApi api;

    // Only timeouts and 5xx are retried; an invalid key or unknown currency won't be fixed by a retry
    @Override
    @Retryable(
            includes = {ResourceAccessException.class, HttpServerErrorException.class},
            maxRetriesString = "${app.twelve-data.max-retries}",
            delayString = "${app.twelve-data.retry-delay}",
            multiplier = 2)
    public BigDecimal getUsdRate(String currency, LocalDate date) {
        TimeSeriesResponse response = api.getTimeSeries("USD/" + currency, "1day", date, 1);
        if (response == null || !response.isOk() || response.values() == null || response.values().isEmpty()) {
            String reason = response == null ? "empty response" : response.message();
            throw new ExchangeRateUnavailableException(
                    "twelvedata has no USD/%s rate on %s: %s".formatted(currency, date, reason));
        }
        BigDecimal currencyPerUsd = response.values().getFirst().close();
        return BigDecimal.ONE.divide(currencyPerUsd, RATE_SCALE, RoundingMode.HALF_UP);
    }
}
