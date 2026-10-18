package org.limir.expensetracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.limir.expensetracker.client.ExchangeRateProvider;
import org.limir.expensetracker.entity.ExchangeRate;
import org.limir.expensetracker.exception.ExchangeRateUnavailableException;
import org.limir.expensetracker.repository.ExchangeRateRepository;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    private static final LocalDate DATE = LocalDate.of(2022, 1, 3);
    private static final BigDecimal KZT_RATE = new BigDecimal("0.0023000000");

    @Mock
    private ExchangeRateRepository repository;

    @Mock
    private ExchangeRateProvider provider;

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    private ExchangeRateService service;

    @BeforeEach
    void setUp() {
        service = new ExchangeRateService(repository, provider, meterRegistry);
    }

    @Test
    void usdRateIsAlwaysOne() {
        assertThat(service.getUsdRate("USD", DATE)).isEqualByComparingTo(BigDecimal.ONE);

        verifyNoInteractions(repository, provider);
    }

    @Test
    void rateFromDatabaseDoesNotCallProvider() {
        when(repository.findByCurrencyAndRateDate("KZT", DATE))
                .thenReturn(Optional.of(new ExchangeRate("KZT", DATE, KZT_RATE)));

        assertThat(service.getUsdRate("KZT", DATE)).isEqualByComparingTo(KZT_RATE);

        verifyNoInteractions(provider);
        assertThat(lookups("database")).isEqualTo(1);
    }

    @Test
    void missingRateIsFetchedFromProviderAndSaved() {
        when(repository.findByCurrencyAndRateDate("KZT", DATE)).thenReturn(Optional.empty());
        when(provider.getUsdRate("KZT", DATE)).thenReturn(KZT_RATE);

        assertThat(service.getUsdRate("KZT", DATE)).isEqualByComparingTo(KZT_RATE);

        verify(repository).insertIfAbsent("KZT", DATE, KZT_RATE);
        assertThat(lookups("provider")).isEqualTo(1);
    }

    @Test
    void providerFailureFallsBackToLastKnownRate() {
        BigDecimal previousRate = new BigDecimal("0.0022000000");
        when(repository.findByCurrencyAndRateDate("KZT", DATE)).thenReturn(Optional.empty());
        when(provider.getUsdRate("KZT", DATE)).thenThrow(new ResourceAccessException("Read timed out"));
        when(repository.findFirstByCurrencyAndRateDateLessThanEqualOrderByRateDateDesc("KZT", DATE))
                .thenReturn(Optional.of(new ExchangeRate("KZT", DATE.minusDays(3), previousRate)));

        assertThat(service.getUsdRate("KZT", DATE)).isEqualByComparingTo(previousRate);

        verify(repository, never()).insertIfAbsent(anyString(), any(), any());
        assertThat(lookups("fallback")).isEqualTo(1);
    }

    @Test
    void providerFailureWithoutKnownRateThrows() {
        when(repository.findByCurrencyAndRateDate("KZT", DATE)).thenReturn(Optional.empty());
        when(provider.getUsdRate("KZT", DATE)).thenThrow(new ExchangeRateUnavailableException("no data"));
        when(repository.findFirstByCurrencyAndRateDateLessThanEqualOrderByRateDateDesc("KZT", DATE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUsdRate("KZT", DATE))
                .isInstanceOf(ExchangeRateUnavailableException.class)
                .hasMessageContaining("KZT");
    }

    private double lookups(String source) {
        return meterRegistry.counter(ExchangeRateService.LOOKUPS_METRIC, "source", source).count();
    }
}
