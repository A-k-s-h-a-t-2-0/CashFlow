package com.example.cashflow

import android.content.res.ColorStateList
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

        applyTheme(ThemeManager.getTheme(requireContext()))
        loadBudget()
    }

    private fun applyTheme(theme: AppTheme) {
        view?.findViewById<View>(R.id.budgetsRootLayout)?.setBackgroundColor(theme.bgDark)
        view?.findViewById<TextView>(R.id.tvBudgetsHeading)?.setTextColor(theme.textLight)
        if (::tvBudgetAmount.isInitialized) {
            tvBudgetAmount.setTextColor(theme.textLight)
        }
        if (::tvBudgetSpent.isInitialized) {
            tvBudgetSpent.setTextColor(theme.lightShade)
        }
        if (::tvBudgetRemaining.isInitialized) {
            tvBudgetRemaining.setTextColor(theme.lightShade)
        }
        if (::budgetProgress.isInitialized) {
            budgetProgress.progressTintList = ColorStateList.valueOf(theme.lightShade)
        }
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
                        transaction.date.startsWith(currentMonth) &&
                        !transaction.excludedFromBudget
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

        if (isAdded) {
            applyTheme(ThemeManager.getTheme(requireContext()))
            if (::db.isInitialized) {
                loadBudget()
            }
        }
    }
}
