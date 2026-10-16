package org.limir.expensetracker.util;

public final class ValidationPatterns {

    public static final String ACCOUNT_NUMBER = "\\d{10}";

    public static final String CURRENCY_CODE = "[A-Z]{3}";

    private ValidationPatterns() {
    }
}
