package com.splitwise.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Group {
    private final String id;
    private final String name;
    private final Map<String, User> members;
    private final List<Expense> expenses;

    public Group(String id, String name, User createdBy) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.members = new LinkedHashMap<>();
        this.expenses = new ArrayList<>();
        addMember(createdBy);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void addMember(User user) {
        members.put(user.getId(), user);
    }

    public boolean hasMember(String userId) {
        return members.containsKey(userId);
    }

    public List<User> getMembers() {
        return List.copyOf(members.values());
    }

    public void addExpense(Expense expense) {
        expenses.add(expense);
    }

    public List<Expense> getExpenses() {
        return List.copyOf(expenses);
    }
}
