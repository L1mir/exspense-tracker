package org.limir.expensetracker.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.limir.expensetracker.IntegrationTest;
import org.limir.expensetracker.MutableClock;
import org.limir.expensetracker.MutableClockConfiguration;
import org.limir.expensetracker.dto.TransactionRequest;
import org.limir.expensetracker.entity.ExchangeRate;
import org.limir.expensetracker.entity.ExpenseCategory;
import org.limir.expensetracker.entity.ExpenseTransaction;
import org.limir.expensetracker.entity.SpendingLimit;
import org.limir.expensetracker.repository.ExchangeRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@IntegrationTest
@Import(MutableClockConfiguration.class)
class TransactionLimitIT {

    private static final String ACCOUNT = "0000000123";

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private LimitService limitService;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Autowired
    private MutableClock clock;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("TRUNCATE expense_transaction, spending_limit, account, exchange_rate");
    }

    @Test
    void case1FromAssignment() {
        setLimit("2022-01-01T00:00:00+06:00", "1000");
        assertThat(pay("2022-01-02T12:00:00+06:00", "500").isLimitExceeded()).isFalse();   // left 500
        ExpenseTransaction jan3 = pay("2022-01-03T12:00:00+06:00", "600");                  // left -100
        setLimit("2022-01-10T00:00:00+06:00", "2000");                                      // left 900
        assertThat(pay("2022-01-11T12:00:00+06:00", "100").isLimitExceeded()).isFalse();   // left 800
        assertThat(pay("2022-01-12T12:00:00+06:00", "700").isLimitExceeded()).isFalse();   // left 100
        assertThat(pay("2022-01-13T12:00:00+06:00", "100").isLimitExceeded()).isFalse();   // left 0
        ExpenseTransaction jan13 = pay("2022-01-13T13:00:00+06:00", "100");                 // left -100

        assertThat(jan3.isLimitExceeded()).isTrue();
        assertExceededLimit(jan3, "1000", "2022-01-01T00:00:00+06:00");
        assertThat(jan13.isLimitExceeded()).isTrue();
        assertExceededLimit(jan13, "2000", "2022-01-10T00:00:00+06:00");
    }

    @Test
    void case2FromAssignmentLoweredLimit() {
        setLimit("2022-02-01T00:00:00+06:00", "1000");
        assertThat(pay("2022-02-02T12:00:00+06:00", "500").isLimitExceeded()).isFalse();   // left 500
        assertThat(pay("2022-02-03T12:00:00+06:00", "100").isLimitExceeded()).isFalse();   // left 400
        setLimit("2022-02-10T00:00:00+06:00", "400");                                       // left -200
        ExpenseTransaction feb11 = pay("2022-02-11T12:00:00+06:00", "100");                 // left -300
        ExpenseTransaction feb12 = pay("2022-02-12T12:00:00+06:00", "100");                 // left -400

        assertThat(feb11.isLimitExceeded()).isTrue();
        assertThat(feb12.isLimitExceeded()).isTrue();
        assertExceededLimit(feb12, "400", "2022-02-10T00:00:00+06:00");
    }

    @Test
    void remainingLimitOfExactlyZeroIsNotExceeded() {
        setLimit("2022-01-01T00:00:00+06:00", "1000");

        assertThat(pay("2022-01-02T12:00:00+06:00", "1000.00").isLimitExceeded()).isFalse();
        assertThat(pay("2022-01-02T13:00:00+06:00", "0.01").isLimitExceeded()).isTrue();
    }

    @Test
    void newMonthStartsWithFullLimit() {
        setLimit("2022-01-01T00:00:00+06:00", "1000");
        assertThat(pay("2022-01-31T23:30:00+06:00", "900").isLimitExceeded()).isFalse();

        // Already February in Almaty, although still January 31 in UTC
        assertThat(pay("2022-02-01T00:30:00+06:00", "900").isLimitExceeded()).isFalse();
    }

    @Test
    void laterLimitDoesNotAffectEarlierTransactions() {
        setLimit("2022-01-01T00:00:00+06:00", "1000");
        ExpenseTransaction jan5 = pay("2022-01-05T12:00:00+06:00", "1200");
        setLimit("2022-01-10T00:00:00+06:00", "5000");

        assertThat(jan5.isLimitExceeded()).isTrue();
        assertExceededLimit(jan5, "1000", "2022-01-01T00:00:00+06:00");
    }

    @Test
    void defaultLimitIs1000UsdFromFirstTransaction() {
        ExpenseTransaction first = pay("2022-01-02T12:00:00+06:00", "600");
        ExpenseTransaction second = pay("2022-01-03T12:00:00+06:00", "500");

        assertThat(first.isLimitExceeded()).isFalse();
        assertThat(second.isLimitExceeded()).isTrue();
        assertExceededLimit(second, "1000", "2022-01-02T12:00:00+06:00");
    }

    @Test
    void categoriesHaveSeparateLimits() {
        assertThat(pay("2022-01-02T12:00:00+06:00", "900", ExpenseCategory.PRODUCT).isLimitExceeded()).isFalse();
        assertThat(pay("2022-01-02T13:00:00+06:00", "900", ExpenseCategory.SERVICE).isLimitExceeded()).isFalse();
    }

    @Test
    void transactionInOtherCurrencyIsConvertedByRateOfItsDay() {
        exchangeRateRepository.save(new ExchangeRate("KZT", LocalDate.of(2022, 1, 3), new BigDecimal("0.0023")));

        ExpenseTransaction transaction = transactionService.process(new TransactionRequest(
                ACCOUNT, "9999999999", "KZT", new BigDecimal("10000.45"), ExpenseCategory.PRODUCT,
                OffsetDateTime.parse("2022-01-03T10:00:00+06:00")));

        // 10000.45 * 0.0023 = 23.001035
        assertThat(transaction.getSumUsd()).isEqualByComparingTo("23.00");
    }

    @Test
    void parallelTransactionsDoNotSeeTheSameRemainingLimit() throws Exception {
        setLimit("2022-01-01T00:00:00+06:00", "1000");

        // 10 transactions of 150 USD at the same time: only 6 fit into 1000 USD
        List<Future<ExpenseTransaction>> results = new ArrayList<>();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 10; i++) {
                results.add(executor.submit(() -> pay("2022-01-05T12:00:00+06:00", "150")));
            }
        }

        long exceeded = 0;
        for (Future<ExpenseTransaction> result : results) {
            if (result.get().isLimitExceeded()) {
                exceeded++;
            }
        }
        assertThat(exceeded).isEqualTo(4);
    }

    private void setLimit(String isoDateTime, String amount) {
        clock.setTime(isoDateTime);
        limitService.setLimit(ACCOUNT, ExpenseCategory.PRODUCT, new BigDecimal(amount));
    }

    private ExpenseTransaction pay(String isoDateTime, String usd) {
        return pay(isoDateTime, usd, ExpenseCategory.PRODUCT);
    }

    private ExpenseTransaction pay(String isoDateTime, String usd, ExpenseCategory category) {
        return transactionService.process(new TransactionRequest(
                ACCOUNT, "9999999999", "USD", new BigDecimal(usd), category, OffsetDateTime.parse(isoDateTime)));
    }

    private static void assertExceededLimit(ExpenseTransaction transaction, String amount, String setAt) {
        SpendingLimit limit = transaction.getSpendingLimit();
        assertThat(limit.getAmount()).isEqualByComparingTo(amount);
        assertThat(limit.getCurrency()).isEqualTo("USD");
        assertThat(limit.getSetAt()).isEqualTo(OffsetDateTime.parse(setAt).toInstant());
    }
}
