package org.limir.expensetracker.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.limir.expensetracker.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    Optional<ExchangeRate> findByCurrencyAndRateDate(String currency, LocalDate rateDate);

    Optional<ExchangeRate> findFirstByCurrencyAndRateDateLessThanEqualOrderByRateDateDesc(String currency,
                                                                                         LocalDate rateDate);

    // ON CONFLICT: two parallel requests may fetch the same rate; the second insert is skipped
    @Transactional
    @Modifying
    @Query(value = """
            INSERT INTO exchange_rate (currency, rate_date, rate)
            VALUES (:currency, :rateDate, :rate)
            ON CONFLICT (currency, rate_date) DO NOTHING
            """, nativeQuery = true)
    void insertIfAbsent(String currency, LocalDate rateDate, BigDecimal rate);
}
