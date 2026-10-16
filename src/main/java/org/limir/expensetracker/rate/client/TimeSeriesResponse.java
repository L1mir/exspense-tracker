package org.limir.expensetracker.rate.client;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Response of {@code /time_series}. On errors twelvedata may answer with HTTP 200 and
 * {@code "status": "error"}, so the status must always be checked.
 */
public record TimeSeriesResponse(String status, Integer code, String message, List<Value> values) {

    public boolean isOk() {
        return "ok".equals(status);
    }

    /** One daily candle; only the close price is used. */
    public record Value(LocalDate datetime, BigDecimal close) {
    }
}
