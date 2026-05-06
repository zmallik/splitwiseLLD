package com.splitwise.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Split {
    private final User user;
    private BigDecimal amount;
    private BigDecimal percent;

    private Split(User user, BigDecimal amount, BigDecimal percent) {
        this.user = Objects.requireNonNull(user);
        this.amount = amount;
        this.percent = percent;
    }

    public static Split forUser(User user) {
        return new Split(user, null, null);
    }

    public static Split exact(User user, BigDecimal amount) {
        return new Split(user, amount.setScale(2, RoundingMode.HALF_UP), null);
    }

    public static Split percent(User user, BigDecimal percent) {
        return new Split(user, null, percent.setScale(2, RoundingMode.HALF_UP));
    }

    public User getUser() {
        return user;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getPercent() {
        return percent;
    }
}
