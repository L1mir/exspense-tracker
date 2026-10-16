package org.limir.expensetracker.client;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ExchangeRateProvider {

    BigDecimal getUsdRate(String currency, LocalDate date);
}
