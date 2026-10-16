package org.limir.expensetracker.rate.client;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.limir.expensetracker.rate.ExchangeRateProvider;
import org.limir.expensetracker.rate.ExchangeRateUnavailableException;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

/**
 * Rates from twelvedata.com. The service has no KZT/USD pair, only USD/KZT,
 * so USD/{currency} is requested and inverted: 1 / 452.7 KZT = 0.0022 USD.
 */
@Component
@RequiredArgsConstructor
public class TwelveDataExchangeRateProvider implements ExchangeRateProvider {

    /** Matches the scale of exchange_rate.rate in the database. */
    private static final int RATE_SCALE = 10;

    private final TwelveDataApi api;

    /**
     * Timeouts and connection errors ({@link ResourceAccessException}) and 5xx responses are retried
     * with a growing delay. Other errors (invalid key, unknown currency) are not: a retry won't help.
     */
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
