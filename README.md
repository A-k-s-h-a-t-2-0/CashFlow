# CashFlow – Budget Management App

CashFlow is an Android-based personal budget management application developed using **Kotlin and XML**. The application allows users to record income and expenses, manage monthly budgets, organize transactions using categories, and store data using **Cloud Firestore**.

The project focuses on implementing core Android development concepts such as Activities, Fragments, RecyclerView, event listeners, layouts, Firebase integration, and persistent cloud data storage.

---

## Features

### 💰 Transaction Management
- Add income and expense transactions.
- Enter:
  - Amount
  - Title
  - Notes
  - Category
- Automatically records the transaction date and timestamp.
- Transactions are displayed from newest to oldest.
- Long-press a transaction to delete it.
- Delete operation is synchronized with Cloud Firestore.

### 📊 Dashboard
The Home screen provides an overview of:

- Current balance
- Monthly budget
- Remaining monthly budget
- Budget progress
- Three most recent transactions

Balance is calculated as:

```text
Balance = Total Income - Total Expenses
