package org.limir.expensetracker.entity;

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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "expense_transaction")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExpenseTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_from")
    private Account accountFrom;

    private String accountTo;

    private String currency;

    private BigDecimal sum;

    @Enumerated(EnumType.STRING)
    private ExpenseCategory category;

    private Instant datetime;

    private BigDecimal sumUsd;

    private boolean limitExceeded;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "limit_id")
    private SpendingLimit spendingLimit;

    @Builder
    public ExpenseTransaction(Account accountFrom, String accountTo, String currency, BigDecimal sum,
                              ExpenseCategory category, Instant datetime, BigDecimal sumUsd,
                              boolean limitExceeded, SpendingLimit spendingLimit) {
        this.accountFrom = accountFrom;
        this.accountTo = accountTo;
        this.currency = currency;
        this.sum = sum;
        this.category = category;
        this.datetime = datetime;
        this.sumUsd = sumUsd;
        this.limitExceeded = limitExceeded;
        this.spendingLimit = spendingLimit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof ExpenseTransaction other && id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
