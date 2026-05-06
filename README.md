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
