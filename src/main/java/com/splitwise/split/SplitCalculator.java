package com.splitwise.split;

import com.splitwise.model.Split;

import java.math.BigDecimal;
import java.util.List;

public interface SplitCalculator {
    List<Split> calculate(BigDecimal totalAmount, List<Split> splits);
}
