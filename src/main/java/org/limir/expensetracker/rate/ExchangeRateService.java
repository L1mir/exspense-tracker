package org.limir.expensetracker.rate;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.limir.expensetracker.common.MoneyUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

/**
 * Exchange rates to USD. Rates are taken from the database first; the paid external provider
 * is called only when the database has no rate for the day, and its answer is saved.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final ExchangeRateRepository repository;
    private final ExchangeRateProvider provider;

    /**
     * Returns how many USD one unit of the currency cost on the given day.
     *
     * @throws ExchangeRateUnavailableException if the provider fails and the database has no earlier rate
     */
    public BigDecimal getUsdRate(String currency, LocalDate date) {
        if (MoneyUtils.USD.equals(currency)) {
            return BigDecimal.ONE;
        }
        return repository.findByCurrencyAndRateDate(currency, date)
                .map(ExchangeRate::getRate)
                .orElseGet(() -> fetchAndSave(currency, date));
    }

    private BigDecimal fetchAndSave(String currency, LocalDate date) {
        try {
            BigDecimal rate = provider.getUsdRate(currency, date);
            repository.insertIfAbsent(currency, date, rate);
            log.info("Fetched {} rate on {} from provider: {}", currency, date, rate);
            return rate;
        } catch (ExchangeRateUnavailableException | RestClientException e) {
            log.warn("Provider failed for {} on {}, falling back to the last known rate: {}",
                    currency, date, e.getMessage());
            return repository.findFirstByCurrencyAndRateDateLessThanEqualOrderByRateDateDesc(currency, date)
                    .map(ExchangeRate::getRate)
                    .orElseThrow(() -> new ExchangeRateUnavailableException(
                            "No %s rate on or before %s".formatted(currency, date), e));
        }
    }
}
