package com.splitwise.model;

public final class ExpenseMetadata {
    private final String notes;

    private ExpenseMetadata(String notes) {
        this.notes = notes;
    }

    public static ExpenseMetadata of(String notes) {
        return new ExpenseMetadata(notes);
    }

    public static ExpenseMetadata empty() {
        return new ExpenseMetadata("");
    }

    public String getNotes() {
        return notes;
    }
}
