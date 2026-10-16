package org.limir.expensetracker.service;

import lombok.RequiredArgsConstructor;
import org.limir.expensetracker.entity.Account;
import org.limir.expensetracker.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    /**
     * Locks the account row until the current DB transaction ends, so parallel requests of one account
     * are processed one by one. MANDATORY: without an outer transaction the lock would be released at once.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public Account lockOrCreate(String number) {
        accountRepository.insertIfAbsent(number);
        return accountRepository.findByNumberForUpdate(number).orElseThrow();
    }
}
