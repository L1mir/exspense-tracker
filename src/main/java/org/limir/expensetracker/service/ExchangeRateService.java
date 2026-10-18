package org.limir.expensetracker.service;

import io.micrometer.core.instrument.MeterRegistry;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.limir.expensetracker.client.ExchangeRateProvider;
import org.limir.expensetracker.exception.ExchangeRateUnavailableException;
import org.limir.expensetracker.repository.ExchangeRateRepository;
import org.limir.expensetracker.util.MoneyUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    static final String LOOKUPS_METRIC = "exchange.rate.lookups";

    private final ExchangeRateRepository repository;
    private final ExchangeRateProvider provider;
    private final MeterRegistry meterRegistry;

    // Database first; the paid provider is called only when the day's rate is missing
    public BigDecimal getUsdRate(String currency, LocalDate date) {
        if (MoneyUtils.USD.equals(currency)) {
            return BigDecimal.ONE;
        }
        RateLookup lookup = repository.findByCurrencyAndRateDate(currency, date)
                .<RateLookup>map(rate -> new RateLookup.FromDatabase(rate.getRate()))
                .orElseGet(() -> fetchAndSave(currency, date));
        countLookup(lookup, currency, date);
        return lookup.rate();
    }

    private RateLookup fetchAndSave(String currency, LocalDate date) {
        try {
            BigDecimal rate = provider.getUsdRate(currency, date);
            repository.insertIfAbsent(currency, date, rate);
            log.info("Fetched {} rate on {} from provider: {}", currency, date, rate);
            return new RateLookup.FromProvider(rate);
        } catch (ExchangeRateUnavailableException | RestClientException e) {
            log.warn("Provider failed for {} on {}: {}", currency, date, e.getMessage());
            return repository.findFirstByCurrencyAndRateDateLessThanEqualOrderByRateDateDesc(currency, date)
                    .map(rate -> new RateLookup.Fallback(rate.getRate(), rate.getRateDate()))
                    .orElseThrow(() -> new ExchangeRateUnavailableException(
                            "No %s rate on or before %s".formatted(currency, date), e));
        }
    }

    private void countLookup(RateLookup lookup, String currency, LocalDate date) {
        String source = switch (lookup) {
            case RateLookup.FromDatabase fromDatabase -> "database";
            case RateLookup.FromProvider fromProvider -> "provider";
            case RateLookup.Fallback fallback -> {
                log.warn("Using {} rate of {} instead of {}", currency, fallback.rateDate(), date);
                yield "fallback";
            }
        };
        meterRegistry.counter(LOOKUPS_METRIC, "source", source).increment();
    }
}
