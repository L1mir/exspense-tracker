package org.limir.expensetracker.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.limir.expensetracker.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface AccountRepository extends JpaRepository<Account, String> {

    // ON CONFLICT: two parallel first requests of a new account must not fail on the primary key
    @Modifying
    @Query(value = "INSERT INTO account (number) VALUES (:number) ON CONFLICT (number) DO NOTHING",
            nativeQuery = true)
    void insertIfAbsent(String number);

    // SELECT ... FOR UPDATE
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.number = :number")
    Optional<Account> findByNumberForUpdate(String number);
}
