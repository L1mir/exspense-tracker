package org.limir.expensetracker.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.limir.expensetracker.config.AppProperties;
import org.limir.expensetracker.dto.TransactionRequest;
import org.limir.expensetracker.entity.Account;
import org.limir.expensetracker.entity.ExpenseTransaction;
import org.limir.expensetracker.entity.SpendingLimit;
import org.limir.expensetracker.repository.ExpenseTransactionRepository;
import org.limir.expensetracker.util.DateTimeUtils;
import org.limir.expensetracker.util.MoneyUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final ExchangeRateService exchangeRateService;
    private final AccountService accountService;
    private final LimitService limitService;
    private final ExpenseTransactionRepository transactionRepository;
    private final TransactionTemplate transactionTemplate;
    private final AppProperties properties;

    public ExpenseTransaction process(TransactionRequest request) {
        Instant moment = request.datetime().toInstant();
        LocalDate day = moment.atZone(properties.timeZone()).toLocalDate();
        BigDecimal rate = exchangeRateService.getUsdRate(request.currencyShortname(), day);
        BigDecimal sumUsd = MoneyUtils.toUsd(request.sum(), rate);

        // The rate is fetched outside the DB transaction, so a slow external API does not hold the account lock
        return transactionTemplate.execute(status -> saveWithLimitCheck(request, moment, sumUsd));
    }

    @Transactional(readOnly = true)
    public List<ExpenseTransaction> getExceeded(String accountNumber) {
        return transactionRepository.findExceeded(accountNumber);
    }

    private ExpenseTransaction saveWithLimitCheck(TransactionRequest request, Instant moment, BigDecimal sumUsd) {
        Account account = accountService.lockOrCreate(request.accountFrom());
        SpendingLimit limit = limitService.getOrCreateActiveLimit(account, request.expenseCategory(), moment);

        BigDecimal spentThisMonth = transactionRepository.sumUsdBetween(
                account.getNumber(),
                request.expenseCategory(),
                DateTimeUtils.startOfMonth(moment, properties.timeZone()),
                DateTimeUtils.startOfNextMonth(moment, properties.timeZone()));
        boolean limitExceeded = spentThisMonth.add(sumUsd).compareTo(limit.getAmount()) > 0;

        ExpenseTransaction transaction = transactionRepository.save(ExpenseTransaction.builder()
                .accountFrom(account)
                .accountTo(request.accountTo())
                .currency(request.currencyShortname())
                .sum(MoneyUtils.toMoney(request.sum()))
                .category(request.expenseCategory())
                .datetime(moment)
                .sumUsd(sumUsd)
                .limitExceeded(limitExceeded)
                .spendingLimit(limit)
                .build());
        log.debug("Account {} {}: spent {} + {} USD, limit {} USD, exceeded={}",
                account.getNumber(), request.expenseCategory(), spentThisMonth, sumUsd, limit.getAmount(),
                limitExceeded);
        return transaction;
    }
}
