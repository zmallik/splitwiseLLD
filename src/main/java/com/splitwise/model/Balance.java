package com.splitwise.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Balance {
    private final String fromUserId;
    private final String toUserId;
    private BigDecimal amount;

    public Balance(String fromUserId, String toUserId) {
        this.fromUserId = Objects.requireNonNull(fromUserId);
        this.toUserId = Objects.requireNonNull(toUserId);
        this.amount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void add(BigDecimal value) {
        amount = amount.add(value).setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isSettled() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public String toString() {
        return fromUserId + " owes " + toUserId + ": " + amount;
    }
}
