package com.example.cashflow

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment :
    Fragment(R.layout.fragment_home) {

    private lateinit var db: FirebaseFirestore

    private lateinit var tvHomeBalance: TextView
    private lateinit var tvHomeBudget: TextView
    private lateinit var tvRecentTransactions: TextView
    private lateinit var homeBudgetProgress: ProgressBar

    private var monthlyBudget = 3000.0

    private val timestampFormat =
        SimpleDateFormat(
            "dd MMM yyyy, HH:mm",
            Locale.getDefault()
        )

    private val dateFormat =
        SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        )

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        db = FirebaseFirestore.getInstance()

        tvHomeBalance =
            view.findViewById(
                R.id.tvHomeBalance
            )

        tvHomeBudget =
            view.findViewById(
                R.id.tvHomeBudget
            )

        tvRecentTransactions =
            view.findViewById(
                R.id.tvRecentTransactions
            )

        homeBudgetProgress =
            view.findViewById(
                R.id.homeBudgetProgress
            )

        val btnAddTransaction =
            view.findViewById<Button>(
                R.id.btnAddTransaction
            )

        btnAddTransaction.setOnClickListener {

            val intent =
                Intent(
                    requireContext(),
                    AddTransactionActivity::class.java
                )

            startActivity(intent)
        }

        loadHomeData()
    }

    private fun loadHomeData() {

        db.collection("settings")
            .document("budget")
            .get()
            .addOnSuccessListener { document ->

                if (document.exists()) {

                    monthlyBudget =
                        document.getDouble(
                            "monthlyBudget"
                        ) ?: 3000.0
                }

                loadTransactions()
            }
            .addOnFailureListener {

                monthlyBudget = 3000.0

                loadTransactions()
            }
    }

    private fun loadTransactions() {

        db.collection("transactions")
            .get()
            .addOnSuccessListener { result ->

                var totalIncome = 0.0
                var totalExpense = 0.0

                val recentTransactions =
                    mutableListOf<Transaction>()

                val currentMonth =
                    SimpleDateFormat(
                        "yyyy-MM",
                        Locale.getDefault()
                    ).format(Date())

                for (document in result) {

                    val transaction =
                        document.toObject(
                            Transaction::class.java
                        )

                    recentTransactions.add(
                        transaction
                    )

                    // Balance uses all transactions
                    if (transaction.type == "income") {

                        totalIncome +=
                            transaction.amount

                    } else {

                        totalExpense +=
                            transaction.amount
                    }
                }

                // Calculate all-time balance
                val balance =
                    totalIncome - totalExpense

                // Calculate only current month's expenses
                var currentMonthExpense = 0.0

                for (transaction in recentTransactions) {

                    if (
                        transaction.type == "expense" &&
                        transaction.date.startsWith(currentMonth)
                    ) {

                        currentMonthExpense +=
                            transaction.amount
                    }
                }

                val budgetRemaining =
                    monthlyBudget - currentMonthExpense

                tvHomeBalance.text =
                    "Balance: ₹${formatAmount(balance)}"

                tvHomeBudget.text =
                    "₹${formatAmount(budgetRemaining)} left of ₹${formatAmount(monthlyBudget)}"

                val progress =
                    (
                            (currentMonthExpense / monthlyBudget) * 100
                            )
                        .toInt()
                        .coerceIn(0, 100)

                homeBudgetProgress.progress =
                    progress

                showRecentTransactions(
                    recentTransactions
                )
            }
            .addOnFailureListener { error ->

                Toast.makeText(
                    requireContext(),
                    "Failed to load home data: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun showRecentTransactions(
        transactions: List<Transaction>
    ) {

        if (transactions.isEmpty()) {

            tvRecentTransactions.text =
                "No transactions yet"

            return
        }

        val recent =
            transactions
                .filter {
                    it.timestamp.isNotEmpty()
                }
                .sortedByDescending {

                    try {
                        timestampFormat.parse(
                            it.timestamp
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                .take(3)

        if (recent.isEmpty()) {

            tvRecentTransactions.text =
                "No recent transactions"

            return
        }

        val textBuilder =
            StringBuilder()

        for (transaction in recent) {

            val sign =
                if (transaction.type == "income") {
                    "+"
                } else {
                    "-"
                }

            textBuilder.append(
                "$sign ₹${formatAmount(transaction.amount)} — ${transaction.title}\n"
            )

            textBuilder.append(
                "${transaction.category} • ${transaction.timestamp}\n\n"
            )
        }

        tvRecentTransactions.text =
            textBuilder
                .toString()
                .trim()
    }

    private fun formatAmount(
        amount: Double
    ): String {

        return if (amount % 1.0 == 0.0) {

            amount
                .toLong()
                .toString()

        } else {

            amount
                .toBigDecimal()
                .toPlainString()
        }
    }

    override fun onResume() {

        super.onResume()

        if (::db.isInitialized) {
            loadHomeData()
        }
    }
}