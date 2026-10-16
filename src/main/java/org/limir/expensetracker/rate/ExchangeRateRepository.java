package org.limir.expensetracker.rate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    Optional<ExchangeRate> findByCurrencyAndRateDate(String currency, LocalDate rateDate);

    /** The latest known rate on or before the date, used when the provider is unavailable. */
    Optional<ExchangeRate> findFirstByCurrencyAndRateDateLessThanEqualOrderByRateDateDesc(String currency,
                                                                                         LocalDate rateDate);

    /**
     * Saves a rate unless one already exists for the currency and date. Two parallel requests may fetch
     * the same rate; ON CONFLICT makes the second insert a no-op instead of a unique constraint error.
     */
    @Transactional
    @Modifying
    @Query(value = """
            INSERT INTO exchange_rate (currency, rate_date, rate)
            VALUES (:currency, :rateDate, :rate)
            ON CONFLICT (currency, rate_date) DO NOTHING
            """, nativeQuery = true)
    void insertIfAbsent(String currency, LocalDate rateDate, BigDecimal rate);
}
