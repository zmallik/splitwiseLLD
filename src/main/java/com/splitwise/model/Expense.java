package com.splitwise.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public final class Expense {
    private final String id;
    private final String groupId;
    private final List<Payment> payments;
    private final String description;
    private final BigDecimal amount;
    private final SplitType splitType;
    private final List<Split> splits;
    private final ExpenseMetadata metadata;
    private final Instant createdAt;

    public Expense(
            String id,
            String groupId,
            List<Payment> payments,
            String description,
            BigDecimal amount,
            SplitType splitType,
            List<Split> splits,
            ExpenseMetadata metadata
    ) {
        this.id = Objects.requireNonNull(id);
        this.groupId = Objects.requireNonNull(groupId);
        this.payments = List.copyOf(payments);
        this.description = Objects.requireNonNull(description);
        this.amount = Objects.requireNonNull(amount);
        this.splitType = Objects.requireNonNull(splitType);
        this.splits = List.copyOf(splits);
        this.metadata = metadata == null ? ExpenseMetadata.empty() : metadata;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getGroupId() {
        return groupId;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public SplitType getSplitType() {
        return splitType;
    }

    public List<Split> getSplits() {
        return splits;
    }

    public ExpenseMetadata getMetadata() {
        return metadata;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
