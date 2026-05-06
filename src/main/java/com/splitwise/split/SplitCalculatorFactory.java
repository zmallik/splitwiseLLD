package com.splitwise.split;

import com.splitwise.exception.SplitwiseException;
import com.splitwise.model.SplitType;

public final class SplitCalculatorFactory {
    private SplitCalculatorFactory() {
    }

    public static SplitCalculator get(SplitType splitType) {
        return switch (splitType) {
            case EQUAL -> new EqualSplitCalculator();
            case EXACT -> new ExactSplitCalculator();
            case PERCENT -> new PercentSplitCalculator();
            default -> throw new SplitwiseException("Unsupported split type: " + splitType);
        };
    }
}
