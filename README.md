# Splitwise LLD Java

Plain Java implementation for the Splitwise low-level design interview question.

## Features

- Users and groups
- Expenses with one or more payers
- `EQUAL`, `EXACT`, and `PERCENT` splits
- Balance sheet tracking
- Group balances
- Overall user balances
- Settlement simplification using min-cash-flow approach
- Runnable demo

## Run

```bash
javac -d out $(find src/main/java -name "*.java")
java -cp out com.splitwise.SplitwiseDemo
```

## Design

- `model`: domain objects such as `User`, `Group`, `Expense`, `Split`, and `Balance`.
- `split`: strategy-style split calculators.
- `service`: application service that validates input and updates balances.
- `exception`: domain exceptions.

## Multi-Payer Expenses

An expense can have one or more payers. The payment side and split side are
handled separately.

Example:

```text
Expense amount = 4000

Payments:
Alice paid 2500
Bob paid 1500

Splits:
Alice owes 1000
Bob owes 1000
Charlie owes 1000
Diana owes 1000
```

For each user:

```text
net = amountPaid - amountOwed
```

So:

```text
Alice   = 2500 - 1000 = +1500
Bob     = 1500 - 1000 = +500
Charlie =    0 - 1000 = -1000
Diana   =    0 - 1000 = -1000
```

Positive net means the user should receive money. Negative net means the user
should pay money.

The service then matches debtors with creditors:

```text
Charlie pays Alice 1000
Diana pays Alice 500
Diana pays Bob 500
```

In code, this logic lives in `SplitwiseService.applyNetPositions(...)`.
