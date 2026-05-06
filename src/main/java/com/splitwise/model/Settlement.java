package com.splitwise.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Settlement {
    private final User from;
    private final User to;
    private final BigDecimal amount;

    public Settlement(User from, User to, BigDecimal amount) {
        this.from = from;
        this.to = to;
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public User getFrom() {
        return from;
    }

    public User getTo() {
        return to;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return from.getName() + " pays " + to.getName() + ": " + amount;
    }
}
