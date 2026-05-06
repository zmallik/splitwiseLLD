package com.splitwise.split;

import com.splitwise.exception.SplitwiseException;
import com.splitwise.model.Split;

import java.math.BigDecimal;
import java.util.List;

public final class ExactSplitCalculator implements SplitCalculator {
    @Override
    public List<Split> calculate(BigDecimal totalAmount, List<Split> splits) {
        BigDecimal sum = splits.stream()
                .map(Split::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (sum.compareTo(totalAmount) != 0) {
            throw new SplitwiseException("Exact split amounts must add up to " + totalAmount + ", found " + sum);
        }

        return splits.stream()
                .map(split -> Split.exact(split.getUser(), split.getAmount()))
                .toList();
    }
}
