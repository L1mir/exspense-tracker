package org.limir.expensetracker.common;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Expense category. Monthly limits are kept separately for each category. */
public enum ExpenseCategory {
    /** In JSON: {@code "product"}. */
    @JsonProperty("product")
    PRODUCT,
    /** In JSON: {@code "service"}. */
    @JsonProperty("service")
    SERVICE
}
