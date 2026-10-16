package org.limir.expensetracker.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.limir.expensetracker.client.ExchangeRateProvider;
import org.limir.expensetracker.entity.ExchangeRate;
import org.limir.expensetracker.exception.ExchangeRateUnavailableException;
import org.limir.expensetracker.repository.ExchangeRateRepository;
import org.limir.expensetracker.util.MoneyUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final ExchangeRateRepository repository;
    private final ExchangeRateProvider provider;

    // Database first; the paid provider is called only when the day's rate is missing
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
