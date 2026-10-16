package org.limir.expensetracker.common;

/** Regular expressions shared by request validation. */
public final class ValidationPatterns {

    /** Bank account number: exactly 10 digits. */
    public static final String ACCOUNT_NUMBER = "\\d{10}";

    /** ISO 4217 currency code: 3 uppercase letters. */
    public static final String CURRENCY_CODE = "[A-Z]{3}";

    private ValidationPatterns() {
    }
}
