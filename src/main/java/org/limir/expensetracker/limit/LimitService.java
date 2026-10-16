package org.limir.expensetracker.limit;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.limir.expensetracker.account.Account;
import org.limir.expensetracker.account.AccountService;
import org.limir.expensetracker.common.ExpenseCategory;
import org.limir.expensetracker.common.MoneyUtils;
import org.limir.expensetracker.config.AppProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LimitService {

    private final SpendingLimitRepository limitRepository;
    private final AccountService accountService;
    private final AppProperties properties;
    private final Clock clock;

    /** Sets a new monthly limit. The date is always the current time: past or future dates are not allowed. */
    @Transactional
    public SpendingLimit setLimit(String accountNumber, ExpenseCategory category, BigDecimal amount) {
        Account account = accountService.lockOrCreate(accountNumber);
        // Seconds are enough for a limit date and keep the API output readable
        Instant now = Instant.now(clock).truncatedTo(ChronoUnit.SECONDS);
        SpendingLimit limit = limitRepository.save(
                new SpendingLimit(account, category, MoneyUtils.toMoney(amount), MoneyUtils.USD, now));
        log.info("Set {} limit {} USD for account {}", category, amount, accountNumber);
        return limit;
    }

    /** All limits of the account, newest first. */
    @Transactional(readOnly = true)
    public List<SpendingLimit> getLimits(String accountNumber) {
        return limitRepository.findAllByAccountNumberOrderBySetAtDesc(accountNumber);
    }

    /**
     * The limit active at the moment. If the client has never set one, the default limit is created,
     * effective from that moment, so it can be referenced by transactions and shown to the client.
     * Must be called while the account is locked.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public SpendingLimit getOrCreateActiveLimit(Account account, ExpenseCategory category, Instant moment) {
        return limitRepository.findActiveLimit(account.getNumber(), category.name(), moment)
                .orElseGet(() -> limitRepository.save(
                        new SpendingLimit(account, category, properties.defaultLimit(), MoneyUtils.USD, moment)));
    }
}
