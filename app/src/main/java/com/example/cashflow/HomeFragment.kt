package com.example.cashflow

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var db: FirebaseFirestore

    private lateinit var tvHomeBalance: TextView
    private lateinit var btnUpdateHomeBalance: Button
    private lateinit var tvHomeBudget: TextView
    private lateinit var tvRecentTransactions: TextView
    private lateinit var homeBudgetProgress: ProgressBar
    private lateinit var btnAddTransaction: Button

    private var monthlyBudget = 3000.0
    private var initialBalance = 0.0
    private var currentNetTransactions = 0.0
    private var currentBalance = 0.0

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        db = FirebaseFirestore.getInstance()

        tvHomeBalance = view.findViewById(R.id.tvHomeBalance)
        btnUpdateHomeBalance = view.findViewById(R.id.btnUpdateHomeBalance)
        tvHomeBudget = view.findViewById(R.id.tvHomeBudget)
        tvRecentTransactions = view.findViewById(R.id.tvRecentTransactions)
        homeBudgetProgress = view.findViewById(R.id.homeBudgetProgress)
        btnAddTransaction = view.findViewById(R.id.btnAddTransaction)

        btnAddTransaction.setOnClickListener {
            val intent = Intent(requireContext(), AddTransactionActivity::class.java)
            startActivity(intent)
        }

        btnUpdateHomeBalance.setOnClickListener {
            showUpdateBalanceDialog()
        }

        tvHomeBalance.setOnClickListener {
            showUpdateBalanceDialog()
        }

        applyTheme(ThemeManager.getTheme(requireContext()))
        loadHomeData()
    }

    private fun applyTheme(theme: AppTheme) {
        view?.findViewById<View>(R.id.homeRootLayout)?.setBackgroundColor(theme.bgDark)
        view?.findViewById<TextView>(R.id.tvHomeGreeting)?.setTextColor(theme.textLight)
        view?.findViewById<TextView>(R.id.tvHomeName)?.setTextColor(theme.lightShade)
        if (::tvHomeBalance.isInitialized) {
            tvHomeBalance.setTextColor(theme.textLight)
        }
        if (::btnUpdateHomeBalance.isInitialized) {
            btnUpdateHomeBalance.backgroundTintList = ColorStateList.valueOf(theme.buttonShade)
            btnUpdateHomeBalance.setTextColor(theme.textLight)
        }
        view?.findViewById<TextView>(R.id.tvHomeBudgetTitle)?.setTextColor(theme.textLight)
        if (::tvHomeBudget.isInitialized) {
            tvHomeBudget.setTextColor(theme.lightShade)
        }
        if (::homeBudgetProgress.isInitialized) {
            homeBudgetProgress.progressTintList = ColorStateList.valueOf(theme.lightShade)
        }
        view?.findViewById<TextView>(R.id.tvHomeRecentTransactionsTitle)?.setTextColor(theme.textLight)
        if (::btnAddTransaction.isInitialized) {
            btnAddTransaction.backgroundTintList = ColorStateList.valueOf(theme.lightShade)
            btnAddTransaction.setTextColor(theme.textDark)
        }
    }

    private fun loadHomeData() {
        db.collection("settings")
            .document("budget")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    monthlyBudget = document.getDouble("monthlyBudget") ?: 3000.0
                }
                loadInitialBalance()
            }
            .addOnFailureListener {
                monthlyBudget = 3000.0
                loadInitialBalance()
            }
    }

    private fun loadInitialBalance() {
        db.collection("settings")
            .document("balance")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    initialBalance = document.getDouble("initialBalance") ?: 0.0
                } else {
                    initialBalance = 0.0
                }
                loadTransactions()
            }
            .addOnFailureListener {
                initialBalance = 0.0
                loadTransactions()
            }
    }

    private fun loadTransactions() {
        db.collection("transactions")
            .get()
            .addOnSuccessListener { result ->
                var totalIncome = 0.0
                var totalExpense = 0.0

                val recentTransactions = mutableListOf<Transaction>()

                val currentMonth = SimpleDateFormat(
                    "yyyy-MM",
                    Locale.getDefault()
                ).format(Date())

                for (document in result) {
                    val transaction = document.toObject(Transaction::class.java)
                    transaction.documentId = document.id
                    recentTransactions.add(transaction)

                    if (transaction.type == "income") {
                        totalIncome += transaction.amount
                    } else {
                        totalExpense += transaction.amount
                    }
                }

                currentNetTransactions = totalIncome - totalExpense
                currentBalance = initialBalance + currentNetTransactions

                // Calculate only current month's expenses, excluding any marked as excludedFromBudget
                var currentMonthExpense = 0.0

                for (transaction in recentTransactions) {
                    if (
                        transaction.type == "expense" &&
                        transaction.date.startsWith(currentMonth) &&
                        !transaction.excludedFromBudget
                    ) {
                        currentMonthExpense += transaction.amount
                    }
                }

                val budgetRemaining = monthlyBudget - currentMonthExpense

                tvHomeBalance.text = "Balance: ₹${formatAmount(currentBalance)}"
                tvHomeBudget.text = "₹${formatAmount(budgetRemaining)} left of ₹${formatAmount(monthlyBudget)}"

                val progress = ((currentMonthExpense / monthlyBudget) * 100)
                    .toInt()
                    .coerceIn(0, 100)

                homeBudgetProgress.progress = progress

                showRecentTransactions(recentTransactions)
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    requireContext(),
                    "Failed to load home data: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun showUpdateBalanceDialog() {
        val context = requireContext()
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 10)
        }

        val tvCurrent = TextView(context).apply {
            text = "Current Balance: ₹${formatAmount(currentBalance)}"
            textSize = 15f
            setPadding(0, 0, 0, 16)
        }
        container.addView(tvCurrent)

        val input = EditText(context).apply {
            hint = "Enter new balance"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
            setText(if (currentBalance != 0.0) formatAmount(currentBalance) else "")
            selectAll()
        }
        container.addView(input)

        AlertDialog.Builder(context)
            .setTitle("Update Balance")
            .setView(container)
            .setPositiveButton("Update") { _, _ ->
                val inputText = input.text.toString().trim()
                val targetBalance = inputText.toDoubleOrNull()
                if (targetBalance == null) {
                    Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                saveUpdatedBalance(targetBalance)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveUpdatedBalance(newBalance: Double) {
        val newInitialBalance = newBalance - currentNetTransactions

        db.collection("settings")
            .document("balance")
            .set(mapOf("initialBalance" to newInitialBalance))
            .addOnSuccessListener {
                initialBalance = newInitialBalance
                currentBalance = newBalance
                tvHomeBalance.text = "Balance: ₹${formatAmount(currentBalance)}"
                Toast.makeText(requireContext(), "Balance updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    requireContext(),
                    "Failed to update balance: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun getTransactionDate(t: Transaction): Date {
        var anchorDate: Date? = null
        if (t.date.isNotEmpty()) {
            try {
                anchorDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(t.date)
            } catch (_: Exception) {}
        }

        if (t.timestamp.isNotEmpty()) {
            try {
                val d = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).parse(t.timestamp)
                if (d != null) {
                    if (anchorDate != null) {
                        val calAnchor = Calendar.getInstance().apply { time = anchorDate }
                        val calD = Calendar.getInstance().apply { time = d }
                        if (calAnchor.get(Calendar.YEAR) == calD.get(Calendar.YEAR) &&
                            calAnchor.get(Calendar.DAY_OF_YEAR) == calD.get(Calendar.DAY_OF_YEAR)) {
                            return d
                        } else {
                            calAnchor.set(Calendar.HOUR_OF_DAY, calD.get(Calendar.HOUR_OF_DAY))
                            calAnchor.set(Calendar.MINUTE, calD.get(Calendar.MINUTE))
                            return calAnchor.time
                        }
                    }
                    return d
                }
            } catch (_: Exception) {}
        }

        if (t.documentId.isNotEmpty()) {
            val parts = t.documentId.split("__")
            if (parts.size >= 3) {
                try {
                    val d = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse("${parts[1]} ${parts[2]}")
                    if (d != null) {
                        if (anchorDate != null) {
                            val calAnchor = Calendar.getInstance().apply { time = anchorDate }
                            val calDoc = Calendar.getInstance().apply { time = d }
                            calAnchor.set(Calendar.HOUR_OF_DAY, calDoc.get(Calendar.HOUR_OF_DAY))
                            calAnchor.set(Calendar.MINUTE, calDoc.get(Calendar.MINUTE))
                            return calAnchor.time
                        }
                        return d
                    }
                } catch (_: Exception) {}
            } else {
                try {
                    val regex = Regex("""(\d{4}-\d{2}-\d{2})[_\s]+(\d{2}:\d{2})""")
                    val match = regex.find(t.documentId)
                    if (match != null) {
                        val d = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse("${match.groupValues[1]} ${match.groupValues[2]}")
                        if (d != null) {
                            if (anchorDate != null) {
                                val calAnchor = Calendar.getInstance().apply { time = anchorDate }
                                val calDoc = Calendar.getInstance().apply { time = d }
                                calAnchor.set(Calendar.HOUR_OF_DAY, calDoc.get(Calendar.HOUR_OF_DAY))
                                calAnchor.set(Calendar.MINUTE, calDoc.get(Calendar.MINUTE))
                                return calAnchor.time
                            }
                            return d
                        }
                    }
                } catch (_: Exception) {}
            }
        }

        return anchorDate ?: Date(0)
    }

    private fun getTransactionTime(t: Transaction): Long {
        return getTransactionDate(t).time
    }

    private fun getEffectiveTimestampString(t: Transaction): String {
        val d = getTransactionDate(t)
        if (d.time != 0L) {
            return SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(d)
        }
        return if (t.date.isNotEmpty()) t.date else ""
    }

    private fun showRecentTransactions(
        transactions: List<Transaction>
    ) {
        if (transactions.isEmpty()) {
            tvRecentTransactions.text = "No transactions yet"
            return
        }

        val recent = transactions
            .sortedByDescending { getTransactionTime(it) }
            .take(3)

        if (recent.isEmpty()) {
            tvRecentTransactions.text = "No recent transactions"
            return
        }

        val textBuilder = SpannableStringBuilder()

        for (transaction in recent) {
            val sign = if (transaction.type == "income") "+" else "-"

            textBuilder.append(
                "$sign ₹${formatAmount(transaction.amount)} — ${transaction.title}\n"
            )

            val timeStr = getEffectiveTimestampString(transaction)
            val excludedNote = if (transaction.excludedFromBudget) " • [Excluded from Budget]" else ""
            val line2 = "${transaction.category} • $timeStr$excludedNote\n\n"
            val line2Start = textBuilder.length
            textBuilder.append(line2)

            val dateIndex = line2.indexOf(timeStr)
            if (dateIndex >= 0 && timeStr.isNotEmpty()) {
                textBuilder.setSpan(
                    StyleSpan(Typeface.BOLD),
                    line2Start + dateIndex,
                    line2Start + dateIndex + timeStr.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        tvRecentTransactions.text = textBuilder
    }

    private fun formatAmount(
        amount: Double
    ): String {
        return if (amount % 1.0 == 0.0) {
            amount.toLong().toString()
        } else {
            amount.toBigDecimal().toPlainString()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isAdded) {
            applyTheme(ThemeManager.getTheme(requireContext()))
            if (::db.isInitialized) {
                loadHomeData()
            }
        }
    }
}
