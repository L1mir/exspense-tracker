package org.limir.expensetracker;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.limir.expensetracker.dto.ExceededTransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

/**
 * "Case 1" from the assignment table through the real HTTP API: limits are set and transactions
 * are sent over HTTP, then the list of exceeded transactions is requested.
 * Transactions are in KZT; the mocked twelvedata returns USD/KZT = 500, so 250 000 KZT = 500 USD.
 */
@IntegrationTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(MutableClockConfiguration.class)
class Case1EndToEndIT {

    private static final String ACCOUNT = "0000000123";

    @RegisterExtension
    static WireMockExtension twelveData = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void twelveDataUrl(DynamicPropertyRegistry registry) {
        registry.add("app.twelve-data.base-url", twelveData::baseUrl);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private RestClient.Builder restClientBuilder;

    @Autowired
    private MutableClock clock;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RestClient api;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE expense_transaction, spending_limit, account, exchange_rate");
        twelveData.stubFor(get(urlPathEqualTo("/time_series")).willReturn(okJson("""
                {"values": [{"datetime": "2022-01-01", "close": "500.00"}], "status": "ok"}
                """)));
        api = restClientBuilder.baseUrl("http://localhost:" + port).build();
    }

    @Test
    void returnsTransactionsOfJanuary3And13WithExceededLimits() {
        setLimit("2022-01-01T00:00:00+06:00", "1000.00");
        sendTransaction("2022-01-02T12:00:00+06:00", "250000.00");   // 500 USD, left 500
        sendTransaction("2022-01-03T12:00:00+06:00", "300000.00");   // 600 USD, left -100: exceeded
        setLimit("2022-01-10T00:00:00+06:00", "2000.00");            // left 900
        sendTransaction("2022-01-11T12:00:00+06:00", "50000.00");    // 100 USD, left 800
        sendTransaction("2022-01-12T12:00:00+06:00", "350000.00");   // 700 USD, left 100
        sendTransaction("2022-01-13T12:00:00+06:00", "50000.00");    // 100 USD, left 0
        sendTransaction("2022-01-13T13:00:00+06:00", "50000.00");    // 100 USD, left -100: exceeded

        List<ExceededTransactionResponse> exceeded = api.get()
                .uri("/client/v1/accounts/{account}/transactions/exceeded", ACCOUNT)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        assertThat(exceeded).hasSize(2);

        ExceededTransactionResponse january3 = exceeded.get(0);
        assertThat(january3.datetime()).isEqualTo(OffsetDateTime.parse("2022-01-03T12:00:00+06:00"));
        assertThat(january3.sum()).isEqualByComparingTo("300000.00");
        assertThat(january3.currencyShortname()).isEqualTo("KZT");
        assertThat(january3.limitSum()).isEqualByComparingTo("1000.00");
        assertThat(january3.limitDatetime()).isEqualTo(OffsetDateTime.parse("2022-01-01T00:00:00+06:00"));
        assertThat(january3.limitCurrencyShortname()).isEqualTo("USD");

        ExceededTransactionResponse january13 = exceeded.get(1);
        assertThat(january13.datetime()).isEqualTo(OffsetDateTime.parse("2022-01-13T13:00:00+06:00"));
        assertThat(january13.sum()).isEqualByComparingTo("50000.00");
        assertThat(january13.limitSum()).isEqualByComparingTo("2000.00");
        assertThat(january13.limitDatetime()).isEqualTo(OffsetDateTime.parse("2022-01-10T00:00:00+06:00"));
        assertThat(january13.limitCurrencyShortname()).isEqualTo("USD");

        // 5 different days: each day's rate is requested once and then read from the database
        twelveData.verify(5, getRequestedFor(urlPathEqualTo("/time_series")));
    }

    private void setLimit(String serverTime, String limitSum) {
        clock.setTime(serverTime);
        api.post()
                .uri("/client/v1/accounts/{account}/limits", ACCOUNT)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("expense_category", "product", "limit_sum", limitSum))
                .retrieve()
                .toBodilessEntity();
    }

    private void sendTransaction(String datetime, String sumKzt) {
        api.post()
                .uri("/integration/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "account_from", ACCOUNT,
                        "account_to", "9999999999",
                        "currency_shortname", "KZT",
                        "sum", sumKzt,
                        "expense_category", "product",
                        "datetime", datetime))
                .retrieve()
                .toBodilessEntity();
    }
}
