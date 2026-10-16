package org.limir.expensetracker.account;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface AccountRepository extends JpaRepository<Account, String> {

    /** Creates the account if it does not exist yet; safe when two requests create it at the same time. */
    @Modifying
    @Query(value = "INSERT INTO account (number) VALUES (:number) ON CONFLICT (number) DO NOTHING",
            nativeQuery = true)
    void insertIfAbsent(String number);

    /**
     * Reads the account with SELECT ... FOR UPDATE. Another transaction asking for the same row
     * waits until the current transaction commits.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.number = :number")
    Optional<Account> findByNumberForUpdate(String number);
}
