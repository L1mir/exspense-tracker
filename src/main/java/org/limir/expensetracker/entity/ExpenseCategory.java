package org.limir.expensetracker.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ExpenseCategory {
    @JsonProperty("product")
    PRODUCT,
    @JsonProperty("service")
    SERVICE
}
