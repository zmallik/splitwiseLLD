package com.splitwise.split;

import com.splitwise.model.Split;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public final class EqualSplitCalculator implements SplitCalculator {
    @Override
    public List<Split> calculate(BigDecimal totalAmount, List<Split> splits) {
        BigDecimal count = BigDecimal.valueOf(splits.size());
        BigDecimal share = totalAmount.divide(count, 2, RoundingMode.DOWN);
        BigDecimal assigned = share.multiply(count);
        BigDecimal remainder = totalAmount.subtract(assigned).setScale(2, RoundingMode.HALF_UP);

        List<Split> result = new ArrayList<>();
        for (int index = 0; index < splits.size(); index++) {
            Split copy = Split.exact(splits.get(index).getUser(), share);
            if (index == 0) {
                copy.setAmount(copy.getAmount().add(remainder));
            }
            result.add(copy);
        }
        return result;
    }
}
