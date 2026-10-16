package org.limir.expensetracker.client;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// twelvedata may report errors with HTTP 200 and "status": "error", so the status is always checked
public record TimeSeriesResponse(String status, Integer code, String message, List<Value> values) {

    public boolean isOk() {
        return "ok".equals(status);
    }

    public record Value(LocalDate datetime, BigDecimal close) {
    }
}
