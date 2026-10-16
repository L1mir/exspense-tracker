package org.limir.expensetracker.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.limir.expensetracker.account.Account;
import org.limir.expensetracker.common.DateTimeMapper;
import org.limir.expensetracker.common.ExpenseCategory;
import org.limir.expensetracker.config.AppProperties;
import org.limir.expensetracker.rate.ExchangeRateUnavailableException;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Web layer only: JSON format, validation and error responses. The service is mocked. */
@WebMvcTest(IntegrationTransactionController.class)
@Import({TransactionMapperImpl.class, DateTimeMapper.class})
@EnableConfigurationProperties(AppProperties.class)
class IntegrationTransactionControllerTest {

    private static final String URL = "/integration/v1/transactions";
    private static final String VALID_REQUEST = """
            {
              "account_from": "0000000123",
              "account_to": "9999999999",
              "currency_shortname": "KZT",
              "sum": 10000.45,
              "expense_category": "product",
              "datetime": "2022-01-30T00:00:00+06:00"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    void receivesTransactionAndReturnsResult() throws Exception {
        when(transactionService.process(any())).thenReturn(ExpenseTransaction.builder()
                .accountFrom(new Account("0000000123"))
                .accountTo("9999999999")
                .currency("KZT")
                .sum(new BigDecimal("10000.45"))
                .category(ExpenseCategory.PRODUCT)
                .datetime(OffsetDateTime.parse("2022-01-30T00:00:00+06:00").toInstant())
                .sumUsd(new BigDecimal("23.00"))
                .limitExceeded(false)
                .build());

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID_REQUEST))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.account_from").value("0000000123"))
                .andExpect(jsonPath("$.currency_shortname").value("KZT"))
                .andExpect(jsonPath("$.expense_category").value("product"))
                .andExpect(jsonPath("$.datetime").value("2022-01-30T00:00:00+06:00"))
                .andExpect(jsonPath("$.sum_usd").value(23.0))
                .andExpect(jsonPath("$.limit_exceeded").value(false));

        ArgumentCaptor<TransactionRequest> request = ArgumentCaptor.forClass(TransactionRequest.class);
        verify(transactionService).process(request.capture());
        assertThat(request.getValue().expenseCategory()).isEqualTo(ExpenseCategory.PRODUCT);
        assertThat(request.getValue().sum()).isEqualByComparingTo("10000.45");
    }

    @Test
    void invalidFieldsReturnProblemDetailWithFieldErrors() throws Exception {
        String invalid = """
                {
                  "account_from": "123",
                  "account_to": "9999999999",
                  "currency_shortname": "kzt",
                  "sum": 10.123,
                  "datetime": "2022-01-30T00:00:00+06:00"
                }
                """;

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.errors.account_from").value("must be 10 digits"))
                .andExpect(jsonPath("$.errors.currency_shortname").exists())
                .andExpect(jsonPath("$.errors.sum").exists())
                .andExpect(jsonPath("$.errors.expense_category").exists());
    }

    @Test
    void unknownCategoryIsBadRequest() throws Exception {
        String request = VALID_REQUEST.replace("\"product\"", "\"food\"");

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void missingExchangeRateReturns503() throws Exception {
        when(transactionService.process(any()))
                .thenThrow(new ExchangeRateUnavailableException("No KZT rate on or before 2022-01-30"));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID_REQUEST))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Exchange rate unavailable"))
                .andExpect(jsonPath("$.detail").value("No KZT rate on or before 2022-01-30"));
    }
}
