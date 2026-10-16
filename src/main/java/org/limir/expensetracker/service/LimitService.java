package org.limir.expensetracker.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.limir.expensetracker.config.AppProperties;
import org.limir.expensetracker.entity.Account;
import org.limir.expensetracker.entity.ExpenseCategory;
import org.limir.expensetracker.entity.SpendingLimit;
import org.limir.expensetracker.repository.SpendingLimitRepository;
import org.limir.expensetracker.util.MoneyUtils;
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

    @Transactional
    public SpendingLimit setLimit(String accountNumber, ExpenseCategory category, BigDecimal amount) {
        Account account = accountService.lockOrCreate(accountNumber);
        Instant now = Instant.now(clock).truncatedTo(ChronoUnit.SECONDS);
        SpendingLimit limit = limitRepository.save(
                new SpendingLimit(account, category, MoneyUtils.toMoney(amount), MoneyUtils.USD, now));
        log.info("Set {} limit {} USD for account {}", category, amount, accountNumber);
        return limit;
    }

    @Transactional(readOnly = true)
    public List<SpendingLimit> getLimits(String accountNumber) {
        return limitRepository.findAllByAccountNumberOrderBySetAtDesc(accountNumber);
    }

    // If no limit was set, the default one is saved from this moment, so the response can show its date
    @Transactional(propagation = Propagation.MANDATORY)
    public SpendingLimit getOrCreateActiveLimit(Account account, ExpenseCategory category, Instant moment) {
        return limitRepository.findActiveLimit(account.getNumber(), category.name(), moment)
                .orElseGet(() -> limitRepository.save(
                        new SpendingLimit(account, category, properties.defaultLimit(), MoneyUtils.USD, moment)));
    }
}
