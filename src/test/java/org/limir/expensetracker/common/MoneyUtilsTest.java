package org.limir.expensetracker.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyUtilsTest {

    @Test
    void convertsAndRoundsToCents() {
        // 10000.45 KZT * 0.0023 = 23.001035 USD
        assertThat(MoneyUtils.toUsd(new BigDecimal("10000.45"), new BigDecimal("0.0023")))
                .isEqualTo(new BigDecimal("23.00"));
    }

    @Test
    void roundsHalfUp() {
        assertThat(MoneyUtils.toUsd(new BigDecimal("0.05"), new BigDecimal("0.1")))
                .isEqualTo(new BigDecimal("0.01"));
    }

    @Test
    void toMoneyAddsCents() {
        assertThat(MoneyUtils.toMoney(new BigDecimal("30"))).isEqualTo(new BigDecimal("30.00"));
    }

    @Test
    void usdRateOfOneKeepsAmount() {
        assertThat(MoneyUtils.toUsd(new BigDecimal("600.00"), BigDecimal.ONE))
                .isEqualTo(new BigDecimal("600.00"));
    }
}
