package com.splitwise.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Payment {
    private final User paidBy;
    private final BigDecimal amount;

    private Payment(User paidBy, BigDecimal amount) {
        this.paidBy = Objects.requireNonNull(paidBy);
        this.amount = Objects.requireNonNull(amount).setScale(2, RoundingMode.HALF_UP);
    }

    public static Payment by(User paidBy, BigDecimal amount) {
        return new Payment(paidBy, amount);
    }

    public User getPaidBy() {
        return paidBy;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
