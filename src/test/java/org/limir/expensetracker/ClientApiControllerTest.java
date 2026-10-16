package org.limir.expensetracker;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.limir.expensetracker.account.Account;
import org.limir.expensetracker.common.DateTimeMapper;
import org.limir.expensetracker.common.ExpenseCategory;
import org.limir.expensetracker.config.AppProperties;
import org.limir.expensetracker.limit.ClientLimitController;
import org.limir.expensetracker.limit.LimitMapperImpl;
import org.limir.expensetracker.limit.LimitService;
import org.limir.expensetracker.limit.SpendingLimit;
import org.limir.expensetracker.transaction.ClientTransactionController;
import org.limir.expensetracker.transaction.ExpenseTransaction;
import org.limir.expensetracker.transaction.TransactionMapperImpl;
import org.limir.expensetracker.transaction.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Web layer of the client API: limits and exceeded transactions. Services are mocked. */
@WebMvcTest({ClientLimitController.class, ClientTransactionController.class})
@Import({LimitMapperImpl.class, TransactionMapperImpl.class, DateTimeMapper.class})
@EnableConfigurationProperties(AppProperties.class)
class ClientApiControllerTest {

    private static final String ACCOUNT = "0000000123";
    private static final Account ACCOUNT_ENTITY = new Account(ACCOUNT);
    private static final SpendingLimit LIMIT_2000 = new SpendingLimit(ACCOUNT_ENTITY, ExpenseCategory.PRODUCT,
            new BigDecimal("2000.00"), "USD", OffsetDateTime.parse("2022-01-10T00:00:00+06:00").toInstant());

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LimitService limitService;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    void setsLimitWithCurrentDate() throws Exception {
        when(limitService.setLimit(ACCOUNT, ExpenseCategory.PRODUCT, new BigDecimal("2000.00"))).thenReturn(LIMIT_2000);

        mockMvc.perform(post("/client/v1/accounts/{account}/limits", ACCOUNT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"expense_category": "product", "limit_sum": 2000.00}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expense_category").value("product"))
                .andExpect(jsonPath("$.limit_sum").value(2000.0))
                .andExpect(jsonPath("$.limit_datetime").value("2022-01-10T00:00:00+06:00"))
                .andExpect(jsonPath("$.limit_currency_shortname").value("USD"));

        verify(limitService).setLimit(ACCOUNT, ExpenseCategory.PRODUCT, new BigDecimal("2000.00"));
    }

    @Test
    void nonPositiveLimitIsRejected() throws Exception {
        mockMvc.perform(post("/client/v1/accounts/{account}/limits", ACCOUNT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"expense_category": "product", "limit_sum": -5}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.limit_sum").exists());
    }

    @Test
    void invalidAccountInPathIsRejected() throws Exception {
        mockMvc.perform(get("/client/v1/accounts/{account}/limits", "123"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors.account").value("must be 10 digits"));
    }

    @Test
    void returnsAllLimits() throws Exception {
        when(limitService.getLimits(ACCOUNT)).thenReturn(List.of(LIMIT_2000));

        mockMvc.perform(get("/client/v1/accounts/{account}/limits", ACCOUNT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].limit_sum").value(2000.0));
    }

    @Test
    void exceededTransactionContainsExceededLimit() throws Exception {
        when(transactionService.getExceeded(ACCOUNT)).thenReturn(List.of(ExpenseTransaction.builder()
                .accountFrom(ACCOUNT_ENTITY)
                .accountTo("9999999999")
                .currency("USD")
                .sum(new BigDecimal("100.00"))
                .category(ExpenseCategory.PRODUCT)
                .datetime(OffsetDateTime.parse("2022-01-13T12:00:00+06:00").toInstant())
                .sumUsd(new BigDecimal("100.00"))
                .limitExceeded(true)
                .spendingLimit(LIMIT_2000)
                .build()));

        mockMvc.perform(get("/client/v1/accounts/{account}/transactions/exceeded", ACCOUNT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].account_from").value(ACCOUNT))
                .andExpect(jsonPath("$[0].datetime").value("2022-01-13T12:00:00+06:00"))
                .andExpect(jsonPath("$[0].limit_sum").value(2000.0))
                .andExpect(jsonPath("$[0].limit_datetime").value("2022-01-10T00:00:00+06:00"))
                .andExpect(jsonPath("$[0].limit_currency_shortname").value("USD"));
    }
}
