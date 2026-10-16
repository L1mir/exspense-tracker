package org.limir.expensetracker.account;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    /**
     * Returns the account, creating it on first use, and locks its row until the current DB transaction ends.
     * All changes of an account's limits and transactions go through this lock, so they run one at a time.
     * Must be called inside a transaction, otherwise the lock would be released immediately.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public Account lockOrCreate(String number) {
        accountRepository.insertIfAbsent(number);
        return accountRepository.findByNumberForUpdate(number).orElseThrow();
    }
}
