package org.limir.expensetracker.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.limir.expensetracker.config.AppProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatePrefetchService {

    private final ExchangeRateService exchangeRateService;
    private final AppProperties properties;
    private final Clock clock;

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "${app.rate-prefetch.cron}", zone = "${app.time-zone}")
    public void prefetchTodayRates() {
        List<String> currencies = properties.ratePrefetch().currencies();
        if (!currencies.isEmpty()) {
            Map<String, BigDecimal> rates = prefetch(currencies, LocalDate.now(clock));
            log.info("Prefetched rates for {} of {} currencies: {}", rates.size(), currencies.size(), rates);
        }
    }

    /**
     * Loads the rates of all currencies at the same time, one virtual thread per currency: the total time
     * is about one external call instead of one call per currency. A failed currency does not stop the others.
     */
    public Map<String, BigDecimal> prefetch(List<String> currencies, LocalDate date) {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Map<String, CompletableFuture<BigDecimal>> futures = currencies.stream()
                    .collect(Collectors.toMap(currency -> currency, currency -> CompletableFuture
                            .supplyAsync(() -> exchangeRateService.getUsdRate(currency, date), executor)
                            .exceptionally(e -> {
                                log.warn("Could not prefetch {} rate on {}: {}", currency, date, e.getMessage());
                                return null;
                            })));
            CompletableFuture.allOf(futures.values().toArray(CompletableFuture[]::new)).join();

            return futures.entrySet().stream()
                    .filter(entry -> entry.getValue().join() != null)
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().join()));
        }
    }
}
