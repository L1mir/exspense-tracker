package org.limir.expensetracker.limit;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.limir.expensetracker.account.Account;
import org.limir.expensetracker.common.ExpenseCategory;

/** Monthly spending limit of an account for one category. Never updated: a new limit is a new row. */
@Entity
@Table(name = "spending_limit")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpendingLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_number")
    private Account account;

    @Enumerated(EnumType.STRING)
    private ExpenseCategory category;

    private BigDecimal amount;

    /** ISO 4217 code, always USD. */
    private String currency;

    private Instant setAt;

    public SpendingLimit(Account account, ExpenseCategory category, BigDecimal amount, String currency, Instant setAt) {
        this.account = account;
        this.category = category;
        this.amount = amount;
        this.currency = currency;
        this.setAt = setAt;
    }

    /** Entities with a generated id are equal only when both are saved and ids match. */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof SpendingLimit other && id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
