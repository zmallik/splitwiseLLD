package com.splitwise.service;

import com.splitwise.exception.SplitwiseException;
import com.splitwise.model.Balance;
import com.splitwise.model.Expense;
import com.splitwise.model.ExpenseMetadata;
import com.splitwise.model.Group;
import com.splitwise.model.Payment;
import com.splitwise.model.Settlement;
import com.splitwise.model.Split;
import com.splitwise.model.SplitType;
import com.splitwise.model.User;
import com.splitwise.split.SplitCalculator;
import com.splitwise.split.SplitCalculatorFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;

public final class SplitwiseService {
    private final Map<String, User> users = new LinkedHashMap<>();
    private final Map<String, Group> groups = new LinkedHashMap<>();
    private final Map<String, Expense> expenses = new LinkedHashMap<>();

    // groupId -> debtorId -> creditorId -> balance
    private final Map<String, Map<String, Map<String, Balance>>> groupBalances = new HashMap<>();

    public User createUser(String id, String name, String email) {
        if (users.containsKey(id)) {
            throw new SplitwiseException("User already exists: " + id);
        }
        User user = new User(id, name, email);
        users.put(id, user);
        return user;
    }

    public Group createGroup(String id, String name, User createdBy) {
        requireKnownUser(createdBy.getId());
        if (groups.containsKey(id)) {
            throw new SplitwiseException("Group already exists: " + id);
        }
        Group group = new Group(id, name, createdBy);
        groups.put(id, group);
        groupBalances.put(id, new HashMap<>());
        return group;
    }

    public void addUserToGroup(String groupId, User user) {
        Group group = requireGroup(groupId);
        requireKnownUser(user.getId());
        group.addMember(user);
    }

    public Expense addExpense(
            String groupId,
            String paidByUserId,
            String description,
            BigDecimal amount,
            SplitType splitType,
            List<Split> splits,
            ExpenseMetadata metadata
    ) {
        User paidBy = requireKnownUser(paidByUserId);
        return addExpense(
                groupId,
                List.of(Payment.by(paidBy, amount)),
                description,
                amount,
                splitType,
                splits,
                metadata
        );
    }

    public Expense addExpense(
            String groupId,
            List<Payment> payments,
            String description,
            BigDecimal amount,
            SplitType splitType,
            List<Split> splits,
            ExpenseMetadata metadata
    ) {
        Group group = requireGroup(groupId);
        validateExpenseInput(group, payments, amount, splits);

        SplitCalculator calculator = SplitCalculatorFactory.get(splitType);
        List<Split> normalizedSplits = calculator.calculate(amount.setScale(2, RoundingMode.HALF_UP), splits);

        Expense expense = new Expense(
                UUID.randomUUID().toString(),
                groupId,
                payments,
                description,
                amount.setScale(2, RoundingMode.HALF_UP),
                splitType,
                normalizedSplits,
                metadata
        );

        applyExpense(groupId, payments, normalizedSplits);
        group.addExpense(expense);
        expenses.put(expense.getId(), expense);
        return expense;
    }

    public List<String> showGroupBalances(String groupId) {
        requireGroup(groupId);
        List<String> result = new ArrayList<>();
        for (Balance balance : allBalances(groupId)) {
            if (!balance.isSettled()) {
                User from = users.get(balance.getFromUserId());
                User to = users.get(balance.getToUserId());
                result.add(from.getName() + " owes " + to.getName() + ": " + balance.getAmount());
            }
        }
        result.sort(String::compareTo);
        return result;
    }

    public List<String> showUserBalances(String userId) {
        requireKnownUser(userId);
        List<String> result = new ArrayList<>();

        for (String groupId : groupBalances.keySet()) {
            for (Balance balance : allBalances(groupId)) {
                if (balance.isSettled()) {
                    continue;
                }
                if (balance.getFromUserId().equals(userId) || balance.getToUserId().equals(userId)) {
                    User from = users.get(balance.getFromUserId());
                    User to = users.get(balance.getToUserId());
                    result.add(from.getName() + " owes " + to.getName() + ": " + balance.getAmount());
                }
            }
        }

        result.sort(String::compareTo);
        return result;
    }

    public List<Settlement> simplifyGroupSettlements(String groupId) {
        Group group = requireGroup(groupId);
        Map<String, BigDecimal> netByUser = new HashMap<>();
        for (User member : group.getMembers()) {
            netByUser.put(member.getId(), BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }

        for (Balance balance : allBalances(groupId)) {
            if (balance.isSettled()) {
                continue;
            }
            netByUser.compute(balance.getFromUserId(), (id, net) -> net.subtract(balance.getAmount()));
            netByUser.compute(balance.getToUserId(), (id, net) -> net.add(balance.getAmount()));
        }

        PriorityQueue<Map.Entry<String, BigDecimal>> debtors = new PriorityQueue<>(Comparator.comparing(Map.Entry::getValue));
        PriorityQueue<Map.Entry<String, BigDecimal>> creditors = new PriorityQueue<>((a, b) -> b.getValue().compareTo(a.getValue()));

        for (Map.Entry<String, BigDecimal> entry : netByUser.entrySet()) {
            int comparison = entry.getValue().compareTo(BigDecimal.ZERO);
            if (comparison < 0) {
                debtors.offer(Map.entry(entry.getKey(), entry.getValue()));
            } else if (comparison > 0) {
                creditors.offer(Map.entry(entry.getKey(), entry.getValue()));
            }
        }

        List<Settlement> settlements = new ArrayList<>();
        while (!debtors.isEmpty() && !creditors.isEmpty()) {
            Map.Entry<String, BigDecimal> debtor = debtors.poll();
            Map.Entry<String, BigDecimal> creditor = creditors.poll();

            BigDecimal debit = debtor.getValue().abs();
            BigDecimal credit = creditor.getValue();
            BigDecimal amount = debit.min(credit).setScale(2, RoundingMode.HALF_UP);

            settlements.add(new Settlement(users.get(debtor.getKey()), users.get(creditor.getKey()), amount));

            BigDecimal remainingDebit = debit.subtract(amount);
            BigDecimal remainingCredit = credit.subtract(amount);

            if (remainingDebit.compareTo(BigDecimal.ZERO) > 0) {
                debtors.offer(Map.entry(debtor.getKey(), remainingDebit.negate()));
            }
            if (remainingCredit.compareTo(BigDecimal.ZERO) > 0) {
                creditors.offer(Map.entry(creditor.getKey(), remainingCredit));
            }
        }

        return settlements;
    }

    private void applyExpense(String groupId, List<Payment> payments, List<Split> splits) {
        Map<String, BigDecimal> netByUser = new HashMap<>();

        for (Payment payment : payments) {
            netByUser.merge(payment.getPaidBy().getId(), payment.getAmount(), BigDecimal::add);
        }

        for (Split split : splits) {
            netByUser.merge(split.getUser().getId(), split.getAmount().negate(), BigDecimal::add);
        }

        applyNetPositions(groupId, netByUser);
    }

    /*
     * Converts per-user net positions for one expense into pairwise balances.
     *
     * net = amountPaid - amountOwed
     *
     * Positive net means the user should receive money.
     * Negative net means the user should pay money.
     *
     * Example:
     * Alice paid 2500, Bob paid 1500, total expense is 4000 split equally
     * across Alice, Bob, Charlie, and Diana.
     *
     * Alice   = 2500 - 1000 = +1500
     * Bob     = 1500 - 1000 = +500
     * Charlie =    0 - 1000 = -1000
     * Diana   =    0 - 1000 = -1000
     *
     * This method separates debtors from creditors and repeatedly matches the
     * largest available debt/credit until all net positions are settled, then
     * records each match with updateBalance(groupId, debtor, creditor, amount).
     */
    private void applyNetPositions(String groupId, Map<String, BigDecimal> netByUser) {
        PriorityQueue<Map.Entry<String, BigDecimal>> debtors = new PriorityQueue<>(Comparator.comparing(Map.Entry::getValue));
        PriorityQueue<Map.Entry<String, BigDecimal>> creditors = new PriorityQueue<>((a, b) -> b.getValue().compareTo(a.getValue()));

        for (Map.Entry<String, BigDecimal> entry : netByUser.entrySet()) {
            int comparison = entry.getValue().compareTo(BigDecimal.ZERO);
            if (comparison < 0) {
                debtors.offer(Map.entry(entry.getKey(), entry.getValue()));
            } else if (comparison > 0) {
                creditors.offer(Map.entry(entry.getKey(), entry.getValue()));
            }
        }

        while (!debtors.isEmpty() && !creditors.isEmpty()) {
            Map.Entry<String, BigDecimal> debtor = debtors.poll();
            Map.Entry<String, BigDecimal> creditor = creditors.poll();

            BigDecimal debit = debtor.getValue().abs();
            BigDecimal credit = creditor.getValue();
            BigDecimal amount = debit.min(credit).setScale(2, RoundingMode.HALF_UP);

            updateBalance(groupId, debtor.getKey(), creditor.getKey(), amount);

            BigDecimal remainingDebit = debit.subtract(amount);
            BigDecimal remainingCredit = credit.subtract(amount);

            if (remainingDebit.compareTo(BigDecimal.ZERO) > 0) {
                debtors.offer(Map.entry(debtor.getKey(), remainingDebit.negate()));
            }
            if (remainingCredit.compareTo(BigDecimal.ZERO) > 0) {
                creditors.offer(Map.entry(creditor.getKey(), remainingCredit));
            }
        }
    }

    private void updateBalance(String groupId, String debtorId, String creditorId, BigDecimal amount) {
        Map<String, Map<String, Balance>> balances = groupBalances.get(groupId);

        Balance reverse = balances
                .getOrDefault(creditorId, Map.of())
                .get(debtorId);

        if (reverse != null && reverse.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal adjustment = reverse.getAmount().min(amount); // 5,  10 => 5
            reverse.add(adjustment.negate());
            amount = amount.subtract(adjustment);
        }

        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        balances
                .computeIfAbsent(debtorId, ignored -> new HashMap<>())
                .computeIfAbsent(creditorId, ignored -> new Balance(debtorId, creditorId))
                .add(amount);
    }

    private Collection<Balance> allBalances(String groupId) {
        return groupBalances.getOrDefault(groupId, Map.of())
                .values()
                .stream()
                .flatMap(inner -> inner.values().stream())
                .toList();
    }

    private void validateExpenseInput(Group group, List<Payment> payments, BigDecimal amount, List<Split> splits) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new SplitwiseException("Expense amount must be positive");
        }
        if (payments == null || payments.isEmpty()) {
            throw new SplitwiseException("Expense must have at least one payment");
        }
        BigDecimal paidAmount = BigDecimal.ZERO;
        for (Payment payment : payments) {
            requireKnownUser(payment.getPaidBy().getId());
            if (!group.hasMember(payment.getPaidBy().getId())) {
                throw new SplitwiseException("Payer is not a group member: " + payment.getPaidBy().getId());
            }
            if (payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new SplitwiseException("Payment amount must be positive");
            }
            paidAmount = paidAmount.add(payment.getAmount());
        }
        if (paidAmount.compareTo(amount.setScale(2, RoundingMode.HALF_UP)) != 0) {
            throw new SplitwiseException("Total paid amount must equal expense amount: " + amount + ", found " + paidAmount);
        }
        if (splits == null || splits.isEmpty()) {
            throw new SplitwiseException("Expense must have at least one split");
        }
        for (Split split : splits) {
            requireKnownUser(split.getUser().getId());
            if (!group.hasMember(split.getUser().getId())) {
                throw new SplitwiseException("Split user is not a group member: " + split.getUser().getId());
            }
        }
    }

    private User requireKnownUser(String userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new SplitwiseException("Unknown user: " + userId);
        }
        return user;
    }

    private Group requireGroup(String groupId) {
        Group group = groups.get(groupId);
        if (group == null) {
            throw new SplitwiseException("Unknown group: " + groupId);
        }
        return group;
    }
}
