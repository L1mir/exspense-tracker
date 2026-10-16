package org.limir.expensetracker.rate;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.limir.expensetracker.IntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.ResourceAccessException;

/** Rates through the real HTTP client against a WireMock stub of twelvedata and a real PostgreSQL. */
@IntegrationTest
class ExchangeRateIT {

    private static final LocalDate DATE = LocalDate.of(2022, 1, 3);
    private static final String USD_KZT_450 = """
            {"meta": {"symbol": "USD/KZT"},
             "values": [{"datetime": "2022-01-03", "close": "450.00000"}],
             "status": "ok"}
            """;

    @RegisterExtension
    static WireMockExtension twelveData = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void twelveDataUrl(DynamicPropertyRegistry registry) {
        registry.add("app.twelve-data.base-url", twelveData::baseUrl);
    }

    @Autowired
    private ExchangeRateProvider provider;

    @Autowired
    private ExchangeRateService service;

    @Autowired
    private ExchangeRateRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void providerRequestsUsdPairAndInvertsClose() {
        twelveData.stubFor(get(urlPathEqualTo("/time_series"))
                .withQueryParam("symbol", equalTo("USD/KZT"))
                .withQueryParam("interval", equalTo("1day"))
                .withQueryParam("end_date", equalTo("2022-01-03"))
                .withQueryParam("outputsize", equalTo("1"))
                .withHeader("Authorization", equalTo("apikey test-key"))
                .willReturn(okJson(USD_KZT_450)));

        BigDecimal rate = provider.getUsdRate("KZT", DATE);

        // 1 / 450 rounded to 10 digits
        assertThat(rate).isEqualByComparingTo("0.0022222222");
    }

    @Test
    void errorStatusInBodyIsNotRetried() {
        twelveData.stubFor(get(urlPathEqualTo("/time_series")).willReturn(okJson("""
                {"code": 400, "message": "No data is available on the specified dates.", "status": "error"}
                """)));

        assertThatThrownBy(() -> provider.getUsdRate("KZT", DATE))
                .isInstanceOf(ExchangeRateUnavailableException.class)
                .hasMessageContaining("No data is available");
        twelveData.verify(1, getRequestedFor(urlPathEqualTo("/time_series")));
    }

    @Test
    void serverErrorIsRetried() {
        twelveData.stubFor(get(urlPathEqualTo("/time_series")).inScenario("unstable")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(serverError())
                .willSetStateTo("recovered"));
        twelveData.stubFor(get(urlPathEqualTo("/time_series")).inScenario("unstable")
                .whenScenarioStateIs("recovered")
                .willReturn(okJson(USD_KZT_450)));

        assertThat(provider.getUsdRate("KZT", DATE)).isEqualByComparingTo("0.0022222222");
        twelveData.verify(2, getRequestedFor(urlPathEqualTo("/time_series")));
    }

    @Test
    void slowResponseTimesOutAndIsRetried() {
        // Read timeout in tests is 1s
        twelveData.stubFor(get(urlPathEqualTo("/time_series"))
                .willReturn(okJson(USD_KZT_450).withFixedDelay(1_500)));

        assertThatThrownBy(() -> provider.getUsdRate("KZT", DATE))
                .isInstanceOf(ResourceAccessException.class);
        // first attempt + 2 retries
        twelveData.verify(3, getRequestedFor(urlPathEqualTo("/time_series")));
    }

    @Test
    void fetchedRateIsSavedAndReusedWithoutCallingProvider() {
        twelveData.stubFor(get(urlPathEqualTo("/time_series")).willReturn(okJson(USD_KZT_450)));

        BigDecimal first = service.getUsdRate("KZT", DATE);
        BigDecimal second = service.getUsdRate("KZT", DATE);

        assertThat(second).isEqualByComparingTo(first);
        assertThat(repository.findByCurrencyAndRateDate("KZT", DATE)).isPresent();
        twelveData.verify(1, getRequestedFor(urlPathEqualTo("/time_series")));
    }

    @Test
    void providerFailureFallsBackToEarlierRateFromDatabase() {
        repository.save(new ExchangeRate("KZT", DATE.minusDays(1), new BigDecimal("0.0021000000")));
        twelveData.stubFor(get(urlPathEqualTo("/time_series")).willReturn(aResponse().withStatus(503)));

        assertThat(service.getUsdRate("KZT", DATE)).isEqualByComparingTo("0.0021");
    }
}
