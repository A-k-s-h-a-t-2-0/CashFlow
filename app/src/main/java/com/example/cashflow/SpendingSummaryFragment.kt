package com.example.cashflow

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class SpendingSummaryFragment :
    Fragment(R.layout.fragment_spending_summary) {

    private lateinit var db: FirebaseFirestore

    private lateinit var tvSummaryIncome: TextView
    private lateinit var tvSummaryExpense: TextView
    private lateinit var tvSummaryBalance: TextView

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        tvSummaryIncome =
            view.findViewById(R.id.tvSummaryIncome)

        tvSummaryExpense =
            view.findViewById(R.id.tvSummaryExpense)

        tvSummaryBalance =
            view.findViewById(R.id.tvSummaryBalance)

        loadSummary()
    }

    private fun loadSummary() {

        db.collection("transactions")
            .get()
            .addOnSuccessListener { result ->

                var totalIncome = 0.0
                var totalExpense = 0.0

                for (document in result) {

                    val transaction =
                        document.toObject(
                            Transaction::class.java
                        )

                    if (transaction.type == "income") {

                        totalIncome +=
                            transaction.amount

                    } else {

                        totalExpense +=
                            transaction.amount
                    }
                }

                val balance =
                    totalIncome - totalExpense

                tvSummaryIncome.text =
                    "Total Income: ₹${formatAmount(totalIncome)}"

                tvSummaryExpense.text =
                    "Total Expenses: ₹${formatAmount(totalExpense)}"

                tvSummaryBalance.text =
                    "Balance: ₹${formatAmount(balance)}"
            }
            .addOnFailureListener { error ->

                Toast.makeText(
                    requireContext(),
                    "Failed to load summary: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun formatAmount(
        amount: Double
    ): String {

        return if (amount % 1.0 == 0.0) {

            amount.toLong().toString()

        } else {

            amount.toBigDecimal()
                .toPlainString()
        }
    }

    override fun onResume() {
        super.onResume()

        if (::db.isInitialized) {
            loadSummary()
        }
    }
}