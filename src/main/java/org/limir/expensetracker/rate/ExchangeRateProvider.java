package org.limir.expensetracker.rate;

import java.math.BigDecimal;
import java.time.LocalDate;

/** External source of exchange rates. To switch providers, implement this interface as a Spring bean. */
public interface ExchangeRateProvider {

    /**
     * Returns how many USD one unit of the currency cost at the close of the given day.
     * If there was no trading that day (weekend, holiday), returns the previous close.
     *
     * @throws ExchangeRateUnavailableException if the provider has no rate or answered with an error
     */
    BigDecimal getUsdRate(String currency, LocalDate date);
}
