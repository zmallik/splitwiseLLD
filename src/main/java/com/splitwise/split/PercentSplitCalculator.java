package com.splitwise.split;

import com.splitwise.exception.SplitwiseException;
import com.splitwise.model.Split;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public final class PercentSplitCalculator implements SplitCalculator {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    @Override
    public List<Split> calculate(BigDecimal totalAmount, List<Split> splits) {
        BigDecimal percentSum = splits.stream()
                .map(Split::getPercent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (percentSum.compareTo(HUNDRED) != 0) {
            throw new SplitwiseException("Percent split must add up to 100, found " + percentSum);
        }

        List<Split> result = new ArrayList<>();
        BigDecimal assigned = BigDecimal.ZERO;
        for (int index = 0; index < splits.size(); index++) {
            Split split = splits.get(index);
            BigDecimal amount = totalAmount
                    .multiply(split.getPercent())
                    .divide(HUNDRED, 2, RoundingMode.DOWN);
            assigned = assigned.add(amount);
            result.add(Split.exact(split.getUser(), amount));
        }

        BigDecimal remainder = totalAmount.subtract(assigned).setScale(2, RoundingMode.HALF_UP);
        if (!result.isEmpty()) {
            result.get(0).setAmount(result.get(0).getAmount().add(remainder));
        }

        return result;
    }
}
