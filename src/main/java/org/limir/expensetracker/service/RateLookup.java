package org.limir.expensetracker.service;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Where an exchange rate came from. Sealed: a switch over it must handle every case. */
sealed interface RateLookup {

    BigDecimal rate();

    record FromDatabase(BigDecimal rate) implements RateLookup {
    }

    record FromProvider(BigDecimal rate) implements RateLookup {
    }

    /** The provider failed; the last known rate of an earlier day was used. */
    record Fallback(BigDecimal rate, LocalDate rateDate) implements RateLookup {
    }
}
