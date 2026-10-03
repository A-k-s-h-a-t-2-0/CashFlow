package com.example.cashflow

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class SettingsFragment :
    Fragment(R.layout.fragment_settings) {

    private lateinit var db: FirebaseFirestore

    private lateinit var etMonthlyBudget: EditText
    private lateinit var btnSaveBudget: Button
    private lateinit var tvCurrentBudget: TextView

    private val defaultBudget = 3000.0

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        db = FirebaseFirestore.getInstance()

        etMonthlyBudget =
            view.findViewById(
                R.id.etMonthlyBudget
            )

        btnSaveBudget =
            view.findViewById(
                R.id.btnSaveBudget
            )

        tvCurrentBudget =
            view.findViewById(
                R.id.tvCurrentBudget
            )

        loadBudget()

        btnSaveBudget.setOnClickListener {
            saveBudget()
        }
    }

    private fun loadBudget() {

        db.collection("settings")
            .document("budget")
            .get()
            .addOnSuccessListener { document ->

                if (document.exists()) {

                    val budget =
                        document.getDouble("monthlyBudget")
                            ?: defaultBudget

                    showBudget(budget)

                } else {

                    showBudget(defaultBudget)
                }
            }
            .addOnFailureListener { error ->

                Toast.makeText(
                    requireContext(),
                    "Failed to load budget: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()

                showBudget(defaultBudget)
            }
    }

    private fun saveBudget() {

        val budgetText =
            etMonthlyBudget
                .text
                .toString()
                .trim()

        if (budgetText.isEmpty()) {

            etMonthlyBudget.error =
                "Enter monthly budget"

            return
        }

        val budget =
            budgetText.toDoubleOrNull()

        if (budget == null || budget <= 0) {

            etMonthlyBudget.error =
                "Enter a valid budget"

            return
        }

        val budgetData =
            hashMapOf(
                "monthlyBudget" to budget
            )

        db.collection("settings")
            .document("budget")
            .set(budgetData)
            .addOnSuccessListener {

                showBudget(budget)

                etMonthlyBudget.text.clear()

                Toast.makeText(
                    requireContext(),
                    "Budget saved successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { error ->

                Toast.makeText(
                    requireContext(),
                    "Failed to save budget: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun showBudget(
        budget: Double
    ) {

        tvCurrentBudget.text =
            "Current Budget: ₹${formatAmount(budget)}"
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
}