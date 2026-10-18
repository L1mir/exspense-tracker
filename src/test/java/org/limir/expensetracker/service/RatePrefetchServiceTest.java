package org.limir.expensetracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.limir.expensetracker.config.AppProperties;
import org.limir.expensetracker.exception.ExchangeRateUnavailableException;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RatePrefetchServiceTest {

    private static final ZoneId ALMATY = ZoneId.of("Asia/Almaty");
    private static final LocalDate DATE = LocalDate.of(2022, 1, 3);

    @Mock
    private ExchangeRateService exchangeRateService;

    private RatePrefetchService service;

    @BeforeEach
    void setUp() {
        // 2022-01-02 20:00 UTC is already January 3 in Almaty
        Clock clock = Clock.fixed(Instant.parse("2022-01-02T20:00:00Z"), ALMATY);
        AppProperties properties = new AppProperties(ALMATY, new BigDecimal("1000"),
                new AppProperties.RatePrefetch(List.of("KZT", "RUB"), "0 5 0 * * *"));
        service = new RatePrefetchService(exchangeRateService, properties, clock);
    }

    @Test
    void ratesAreLoadedInParallel() {
        List<String> currencies = List.of("KZT", "RUB", "EUR");
        // Each call waits until all 3 calls have started: sequential loading would never get past the first one
        CountDownLatch allStarted = new CountDownLatch(currencies.size());
        when(exchangeRateService.getUsdRate(anyString(), eq(DATE))).thenAnswer(invocation -> {
            allStarted.countDown();
            if (!allStarted.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("calls were not made in parallel");
            }
            return BigDecimal.ONE;
        });

        assertThat(service.prefetch(currencies, DATE)).containsOnlyKeys("KZT", "RUB", "EUR");
    }

    @Test
    void failedCurrencyDoesNotStopOthers() {
        when(exchangeRateService.getUsdRate("KZT", DATE)).thenThrow(new ExchangeRateUnavailableException("down"));
        when(exchangeRateService.getUsdRate("RUB", DATE)).thenReturn(new BigDecimal("0.0134"));

        assertThat(service.prefetch(List.of("KZT", "RUB"), DATE))
                .containsOnlyKeys("RUB")
                .containsEntry("RUB", new BigDecimal("0.0134"));
    }

    @Test
    void scheduledRunLoadsConfiguredCurrenciesForTodayInServiceTimeZone() {
        when(exchangeRateService.getUsdRate(anyString(), eq(DATE))).thenReturn(BigDecimal.ONE);

        service.prefetchTodayRates();

        verify(exchangeRateService).getUsdRate("KZT", DATE);
        verify(exchangeRateService).getUsdRate("RUB", DATE);
    }
}
