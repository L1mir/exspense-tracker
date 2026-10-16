package org.limir.expensetracker.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Money constants and calculations. */
public final class MoneyUtils {

    /** Currency of limits and of all USD amounts in the service. */
    public static final String USD = "USD";

    /** Money amounts are stored with 2 decimal places. */
    public static final int MONEY_SCALE = 2;

    private MoneyUtils() {
    }

    /**
     * Converts an amount to USD.
     *
     * @param amount  amount in the original currency
     * @param usdRate how many USD one unit of the original currency costs
     * @return USD amount rounded to cents, half up (0.005 becomes 0.01)
     */
    public static BigDecimal toUsd(BigDecimal amount, BigDecimal usdRate) {
        return amount.multiply(usdRate).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
