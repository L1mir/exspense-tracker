package org.limir.expensetracker.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtils {

    public static final String USD = "USD";

    public static final int MONEY_SCALE = 2;

    private MoneyUtils() {
    }

    public static BigDecimal toMoney(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal toUsd(BigDecimal amount, BigDecimal usdRate) {
        return amount.multiply(usdRate).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
