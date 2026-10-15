package org.limir.expensetracker.account;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Client bank account. Its row is locked while a transaction of the account is processed. */
@Entity
@Table(name = "account")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

    /** 10-digit account number. */
    @Id
    private String number;

    public Account(String number) {
        this.number = number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Account other && Objects.equals(number, other.getNumber());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(number);
    }
}
