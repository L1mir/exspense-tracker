---
name: write-limit-test
description: Write or extend tests for the monthly limit logic of this project (the limit_exceeded flag, default 1000 USD limit, limit changes within a month, month boundaries in Asia/Almaty, parallel transactions). Use when asked to test a limit scenario, reproduce a case from the assignment table, or cover a bug in TransactionService / LimitService.
---

# Writing a limit test in expense-tracker

The limit logic lives in `TransactionService.process()` and `LimitService`
(`src/main/java/org/limir/expensetracker/service/`). It is tested with integration tests on a real
PostgreSQL, because the result depends on SQL (active limit query, month sum) and on row locking.

## Where to put the test

- Service-level scenario (most cases): add a `@Test` method to
  `src/test/java/org/limir/expensetracker/service/TransactionLimitIT.java`. Reuse its helpers, don't create a new class.
- Scenario through the HTTP API: follow `src/test/java/org/limir/expensetracker/Case1EndToEndIT.java`.
- Pure calculation (rounding, month boundaries): unit test next to `util/MoneyUtilsTest` or `util/DateTimeUtilsTest`.

Integration test classes must end with `IT` (run by failsafe), unit tests with `Test` (run by surefire).

## Conventions in TransactionLimitIT

- The class is annotated with `@IntegrationTest` (Spring context + `test` profile + PostgreSQL in Testcontainers)
  and `@Import(MutableClockConfiguration.class)`. Never use a bare `@SpringBootTest`.
- `@BeforeEach` truncates `expense_transaction, spending_limit, account, exchange_rate`, so every test starts empty.
- Helpers:
  - `setLimit(isoDateTime, amount)` moves `MutableClock` to that moment and calls `LimitService.setLimit`.
    The limit date always comes from the clock: never pass a date to the service.
  - `pay(isoDateTime, usd)` / `pay(isoDateTime, usd, category)` sends a USD transaction (rate 1, no external API).
  - `assertExceededLimit(transaction, amount, setAt)` checks which limit a transaction exceeded.
- Use dates from 2022 with an explicit offset `+06:00` (Almaty in 2022), e.g. `"2022-01-13T12:00:00+06:00"`.
- Write one scenario per test, line by line like the assignment table, with the remaining limit in a comment:

```java
@Test
void limitLoweredBelowSpentAmountMarksNextTransaction() {
    setLimit("2022-03-01T00:00:00+06:00", "1000");
    assertThat(pay("2022-03-02T12:00:00+06:00", "700").isLimitExceeded()).isFalse();   // left 300
    setLimit("2022-03-05T00:00:00+06:00", "500");                                       // left -200
    ExpenseTransaction mar6 = pay("2022-03-06T12:00:00+06:00", "10");                   // left -210

    assertThat(mar6.isLimitExceeded()).isTrue();
    assertExceededLimit(mar6, "500", "2022-03-05T00:00:00+06:00");
}
```

## Rules of the logic to keep in mind

- Remaining = active limit - sum of USD spent in the same category in the calendar month (Asia/Almaty).
- `limit_exceeded` is true only when the remaining goes **below** zero; exactly 0 is false.
- The active limit is the latest one with `set_at <= transaction datetime`. Without any limit,
  a default 1000 USD limit is created with `set_at` = the first transaction's datetime.
- Product and service categories have separate limits.
- Flags of earlier transactions are never recalculated.

## Non-USD transactions

Save the rate first, then call `transactionService.process(...)` with the currency:

```java
exchangeRateRepository.save(new ExchangeRate("KZT", LocalDate.of(2022, 1, 3), new BigDecimal("0.0023")));
```

For HTTP-level tests the rate comes from the WireMock stub of twelvedata (see `Case1EndToEndIT.setUp`).

## Run

Docker must be running (Testcontainers).

```bash
./mvnw verify -Dtest=none -Dsurefire.failIfNoSpecifiedTests=false -Dit.test=TransactionLimitIT
```

Before finishing, run the whole suite: `./mvnw verify`.
