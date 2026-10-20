package com.example.cashflow

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TransactionsFragment : Fragment(R.layout.fragment_transactions) {

    private lateinit var db: FirebaseFirestore
    private lateinit var rvTransactions: RecyclerView
    private lateinit var layoutMonthChips: LinearLayout

    private val allTransactions = mutableListOf<Transaction>()
    private val displayItems = mutableListOf<TransactionListItem>()
    private lateinit var adapter: TransactionAdapter

    // null means "All Months"
    private var selectedMonthKey: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        rvTransactions = view.findViewById(R.id.rvTransactions)
        layoutMonthChips = view.findViewById(R.id.layoutMonthChips)

        adapter = TransactionAdapter(displayItems) { transaction ->
            showTransactionOptionsDialog(transaction)
        }

        rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        rvTransactions.adapter = adapter

        applyTheme(ThemeManager.getTheme(requireContext()))
        loadTransactions()
    }

    override fun onResume() {
        super.onResume()
        if (isAdded) {
            applyTheme(ThemeManager.getTheme(requireContext()))
            if (::db.isInitialized) {
                loadTransactions()
            }
        }
    }

    private fun applyTheme(theme: AppTheme) {
        view?.findViewById<View>(R.id.transactionsRootLayout)?.setBackgroundColor(theme.bgDark)
        view?.findViewById<TextView>(R.id.tvTransactionsHeading)?.setTextColor(theme.textLight)
        updateMonthChipsStyle(theme)
        if (::adapter.isInitialized) {
            adapter.notifyDataSetChanged()
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
            }
        }

        return anchorDate ?: Date(0)
    }

    private fun getTransactionTime(t: Transaction): Long {
        return getTransactionDate(t).time
    }

    private fun getMonthKey(t: Transaction): String {
        val d = getTransactionDate(t)
        return if (d.time > 0L) {
            SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(d)
        } else if (t.date.length >= 7) {
            t.date.substring(0, 7)
        } else {
            "Unknown"
        }
    }

    private fun formatMonthDisplay(monthKey: String): String {
        return try {
            val d = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(monthKey)
            if (d != null) {
                SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(d)
            } else {
                monthKey
            }
        } catch (_: Exception) {
            monthKey
        }
    }

    private fun getEffectiveTimestampString(t: Transaction): String {
        val d = getTransactionDate(t)
        if (d.time != 0L) {
            return SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(d)
        }
        return if (t.date.isNotEmpty()) t.date else ""
    }

    private fun loadTransactions() {
        db.collection("transactions")
            .get()
            .addOnSuccessListener { result ->
                allTransactions.clear()

                for (document in result) {
                    val transaction = document.toObject(Transaction::class.java)
                    transaction.documentId = document.id
                    allTransactions.add(transaction)
                }

                // Newest → oldest using getTransactionTime
                allTransactions.sortWith(
                    compareByDescending<Transaction> {
                        getTransactionTime(it)
                    }
                )

                setupMonthChips()
                buildAndApplyDisplayList()
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    requireContext(),
                    "Failed to load transactions: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun setupMonthChips() {
        if (!isAdded) return
        layoutMonthChips.removeAllViews()

        // Get distinct months preserving order (newest first)
        val monthKeys = allTransactions.map { getMonthKey(it) }.distinct()

        // "All Months" chip
        addMonthChip(label = "All Months", monthKey = null)

        for (mk in monthKeys) {
            if (mk != "Unknown") {
                addMonthChip(label = formatMonthDisplay(mk), monthKey = mk)
            }
        }

        updateMonthChipsStyle(ThemeManager.getTheme(requireContext()))
    }

    private fun addMonthChip(label: String, monthKey: String?) {
        val context = requireContext()
        val tvChip = TextView(context).apply {
            text = label
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(32, 16, 32, 16)
            tag = monthKey ?: "ALL"

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = 16
            }
            this.layoutParams = layoutParams

            setOnClickListener {
                selectedMonthKey = monthKey
                updateMonthChipsStyle(ThemeManager.getTheme(context))
                buildAndApplyDisplayList()
            }
        }
        layoutMonthChips.addView(tvChip)
    }

    private fun updateMonthChipsStyle(theme: AppTheme) {
        val childCount = layoutMonthChips.childCount
        for (i in 0 until childCount) {
            val tv = layoutMonthChips.getChildAt(i) as? TextView ?: continue
            val chipKey = tv.tag as? String
            val isSelected = if (selectedMonthKey == null) {
                chipKey == "ALL"
            } else {
                chipKey == selectedMonthKey
            }

            val shape = GradientDrawable().apply {
                cornerRadius = 24f
                // Keep box color same as background shade i.e. the darker shade
                setColor(theme.bgDark)
                if (isSelected) {
                    setStroke(4, theme.lightShade)
                } else {
                    setStroke(2, theme.buttonShade)
                }
            }
            tv.background = shape
            tv.setTextColor(if (isSelected) theme.lightShade else theme.textLight)
        }
    }

    private fun buildAndApplyDisplayList() {
        displayItems.clear()

        if (allTransactions.isEmpty()) {
            adapter.setItems(displayItems)
            return
        }

        // Group by month key
        val groupedByMonth = allTransactions.groupBy { getMonthKey(it) }

        // Filter if specific month is selected
        val targetMonths = if (selectedMonthKey != null) {
            listOfNotNull(selectedMonthKey).filter { groupedByMonth.containsKey(it) }
        } else {
            groupedByMonth.keys.toList()
        }

        for (monthKey in targetMonths) {
            val monthTxList = groupedByMonth[monthKey] ?: continue
            val displayMonth = formatMonthDisplay(monthKey)

            var totalExpense = 0.0
            var totalIncome = 0.0
            var totalExcludedExpense = 0.0

            for (tx in monthTxList) {
                if (tx.type == "expense") {
                    totalExpense += tx.amount
                    if (tx.excludedFromBudget) {
                        totalExcludedExpense += tx.amount
                    }
                } else {
                    totalIncome += tx.amount
                }
            }

            // Month Header (Monthly Bifurcation)
            displayItems.add(
                TransactionListItem.MonthHeader(
                    monthKey = monthKey,
                    monthDisplay = displayMonth,
                    totalExpense = totalExpense,
                    totalIncome = totalIncome,
                    count = monthTxList.size,
                    totalExcludedExpense = totalExcludedExpense
                )
            )

            // Items in this month
            for (tx in monthTxList) {
                displayItems.add(TransactionListItem.Entry(tx))
            }
        }

        adapter.setItems(displayItems)
    }

    private fun showTransactionOptionsDialog(transaction: Transaction) {
        val excludeOption = if (transaction.excludedFromBudget) {
            "Include in Budget Expense"
        } else {
            "Exclude from Budget Expense"
        }
        val options = arrayOf("Edit Transaction", "Delete Transaction", excludeOption)

        AlertDialog.Builder(requireContext())
            .setTitle(transaction.title.ifEmpty { "Transaction Options" })
            .setItems(options) { _, which ->
                when (which) {
                    0 -> editTransaction(transaction)
                    1 -> showDeleteDialog(transaction)
                    2 -> toggleExcludeFromBudget(transaction)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun formatAmount(amount: Double): String {
        return if (amount % 1.0 == 0.0) {
            amount.toLong().toString()
        } else {
            String.format(Locale.getDefault(), "%.2f", amount)
        }
    }

    private fun toggleExcludeFromBudget(transaction: Transaction) {
        if (transaction.documentId.isEmpty()) {
            Toast.makeText(requireContext(), "Missing document ID", Toast.LENGTH_SHORT).show()
            return
        }

        val targetState = !transaction.excludedFromBudget
        if (!targetState && transaction.type == "expense") {
            checkBudgetAndToggle(transaction, targetState)
        } else {
            executeToggleExclude(transaction, targetState)
        }
    }

    private fun checkBudgetAndToggle(transaction: Transaction, targetState: Boolean) {
        db.collection("settings").document("budget").get()
            .addOnSuccessListener { budgetDoc ->
                val monthlyBudget = budgetDoc.getDouble("monthlyBudget") ?: 3000.0
                val targetMonth = getMonthKey(transaction)

                var existingSpent = 0.0
                for (t in allTransactions) {
                    if (t.documentId == transaction.documentId) continue
                    if (t.type == "expense" && getMonthKey(t) == targetMonth && !t.excludedFromBudget) {
                        existingSpent += t.amount
                    }
                }

                val newTotalSpent = existingSpent + transaction.amount
                if (newTotalSpent > monthlyBudget) {
                    val theme = ThemeManager.getTheme(requireContext())
                    val exceededBy = newTotalSpent - monthlyBudget
                    val msg = "Note: Including this transaction will exceed your monthly budget!\n\n" +
                            "• Monthly Budget: ₹${formatAmount(monthlyBudget)}\n" +
                            "• Current Spent: ₹${formatAmount(existingSpent)}\n" +
                            "• New Total: ₹${formatAmount(newTotalSpent)}\n" +
                            "• Exceeds Budget by: ₹${formatAmount(exceededBy)}\n\n" +
                            "Do you want to continue anyway?"

                    val dialog = AlertDialog.Builder(requireContext())
                        .setTitle("⚠️ Budget Warning")
                        .setMessage(msg)
                        .setPositiveButton("Continue Anyway") { _, _ ->
                            executeToggleExclude(transaction, targetState)
                        }
                        .setNegativeButton("Cancel", null)
                        .create()

                    dialog.setOnShowListener {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(theme.lightShade)
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(theme.textLight)
                        dialog.window?.setBackgroundDrawable(
                            android.graphics.drawable.ColorDrawable(theme.cardDark)
                        )
                        dialog.findViewById<TextView>(android.R.id.message)?.setTextColor(theme.textLight)
                        dialog.findViewById<TextView>(androidx.appcompat.R.id.alertTitle)?.setTextColor(theme.lightShade)
                    }
                    dialog.show()
                } else {
                    executeToggleExclude(transaction, targetState)
                }
            }
            .addOnFailureListener {
                executeToggleExclude(transaction, targetState)
            }
    }

    private fun executeToggleExclude(transaction: Transaction, targetState: Boolean) {
        db.collection("transactions")
            .document(transaction.documentId)
            .update("excludedFromBudget", targetState)
            .addOnSuccessListener {
                transaction.excludedFromBudget = targetState
                buildAndApplyDisplayList()
                val msg = if (targetState) {
                    "\"${transaction.title}\" excluded from budget expense"
                } else {
                    "\"${transaction.title}\" included in budget expense"
                }
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    requireContext(),
                    "Failed to update: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun editTransaction(transaction: Transaction) {
        val effectiveTimestamp = getEffectiveTimestampString(transaction)
        val intent = Intent(requireContext(), AddTransactionActivity::class.java).apply {
            putExtra("isEditMode", true)
            putExtra("documentId", transaction.documentId)
            putExtra("amount", transaction.amount)
            putExtra("title", transaction.title)
            putExtra("notes", transaction.notes)
            putExtra("category", transaction.category)
            putExtra("type", transaction.type)
            putExtra("date", transaction.date)
            putExtra("timestamp", effectiveTimestamp)
            putExtra("excludedFromBudget", transaction.excludedFromBudget)
        }
        startActivity(intent)
    }

    private fun showDeleteDialog(transaction: Transaction) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete \"${transaction.title}\"?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                deleteTransaction(transaction)
            }
            .show()
    }

    private fun deleteTransaction(transaction: Transaction) {
        if (transaction.documentId.isEmpty()) {
            Toast.makeText(requireContext(), "Missing document ID", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("transactions")
            .document(transaction.documentId)
            .delete()
            .addOnSuccessListener {
                allTransactions.remove(transaction)
                setupMonthChips()
                buildAndApplyDisplayList()
                Toast.makeText(requireContext(), "Transaction deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    requireContext(),
                    "Failed to delete: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}
