package com.splitwise;

import com.splitwise.model.ExpenseMetadata;
import com.splitwise.model.Group;
import com.splitwise.model.Payment;
import com.splitwise.model.Split;
import com.splitwise.model.SplitType;
import com.splitwise.model.User;
import com.splitwise.service.SplitwiseService;

import java.math.BigDecimal;
import java.util.List;

public class SplitwiseDemo {
    public static void main(String[] args) {
        SplitwiseService splitwise = new SplitwiseService();

        User alice = splitwise.createUser("u1", "Alice", "alice@example.com");
        User bob = splitwise.createUser("u2", "Bob", "bob@example.com");
        User charlie = splitwise.createUser("u3", "Charlie", "charlie@example.com");
        User diana = splitwise.createUser("u4", "Diana", "diana@example.com");

        Group trip = splitwise.createGroup("g1", "Goa Trip", alice);
        splitwise.addUserToGroup(trip.getId(), bob);
        splitwise.addUserToGroup(trip.getId(), charlie);
        splitwise.addUserToGroup(trip.getId(), diana);

        splitwise.addExpense(
                trip.getId(),
                alice.getId(),
                "Hotel",
                BigDecimal.valueOf(4000),
                SplitType.EQUAL,
                List.of(
                        Split.forUser(alice),
                        Split.forUser(bob),
                        Split.forUser(charlie),
                        Split.forUser(diana)
                ),
                ExpenseMetadata.of("2 nights stay")
        );

        splitwise.addExpense(
                trip.getId(),
                bob.getId(),
                "Dinner",
                BigDecimal.valueOf(2400),
                SplitType.EXACT,
                List.of(
                        Split.exact(alice, BigDecimal.valueOf(800)),
                        Split.exact(bob, BigDecimal.valueOf(600)),
                        Split.exact(charlie, BigDecimal.valueOf(600)),
                        Split.exact(diana, BigDecimal.valueOf(400))
                ),
                ExpenseMetadata.empty()
        );

        splitwise.addExpense(
                trip.getId(),
                List.of(
                        Payment.by(charlie, BigDecimal.valueOf(600)),
                        Payment.by(diana, BigDecimal.valueOf(400))
                ),
                "Cab",
                BigDecimal.valueOf(1000),
                SplitType.PERCENT,
                List.of(
                        Split.percent(alice, BigDecimal.valueOf(40)),
                        Split.percent(bob, BigDecimal.valueOf(20)),
                        Split.percent(charlie, BigDecimal.valueOf(20)),
                        Split.percent(diana, BigDecimal.valueOf(20))
                ),
                ExpenseMetadata.empty()
        );

        System.out.println("Group balances:");
        splitwise.showGroupBalances(trip.getId()).forEach(System.out::println);

        System.out.println();
        System.out.println("Simplified settlements:");
        splitwise.simplifyGroupSettlements(trip.getId()).forEach(System.out::println);
    }
}
