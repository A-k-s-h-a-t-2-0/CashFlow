package com.example.cashflow

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BudgetsFragment :
    Fragment(R.layout.fragment_budgets) {

    private lateinit var db: FirebaseFirestore

    private lateinit var tvBudgetAmount: TextView
    private lateinit var tvBudgetSpent: TextView
    private lateinit var tvBudgetRemaining: TextView
    private lateinit var budgetProgress: ProgressBar

    private var monthlyBudget = 3000.0

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        db = FirebaseFirestore.getInstance()

        tvBudgetAmount =
            view.findViewById(
                R.id.tvBudgetAmount
            )

        tvBudgetSpent =
            view.findViewById(
                R.id.tvBudgetSpent
            )

        tvBudgetRemaining =
            view.findViewById(
                R.id.tvBudgetRemaining
            )

        budgetProgress =
            view.findViewById(
                R.id.budgetProgress
            )

        loadBudget()
    }

    private fun loadBudget() {

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

                tvBudgetAmount.text =
                    "Monthly Budget: ₹${formatAmount(monthlyBudget)}"

                loadBudgetData()
            }
            .addOnFailureListener {

                monthlyBudget = 3000.0

                tvBudgetAmount.text =
                    "Monthly Budget: ₹${formatAmount(monthlyBudget)}"

                loadBudgetData()
            }
    }

    private fun loadBudgetData() {

        db.collection("transactions")
            .get()
            .addOnSuccessListener { result ->

                var totalSpent = 0.0

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

                    if (
                        transaction.type == "expense" &&
                        transaction.date.startsWith(currentMonth)
                    ) {

                        totalSpent +=
                            transaction.amount
                    }
                }

                val remaining =
                    monthlyBudget - totalSpent

                tvBudgetSpent.text =
                    "Total Spent: ₹${formatAmount(totalSpent)}"

                tvBudgetRemaining.text =
                    "Remaining: ₹${formatAmount(remaining)}"

                val progress =
                    (
                            (totalSpent / monthlyBudget) * 100
                            )
                        .toInt()
                        .coerceIn(0, 100)

                budgetProgress.progress =
                    progress
            }
            .addOnFailureListener { error ->

                Toast.makeText(
                    requireContext(),
                    "Failed to load budget: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
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
            loadBudget()
        }
    }
}