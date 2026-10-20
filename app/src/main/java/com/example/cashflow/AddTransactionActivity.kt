package com.example.cashflow

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    private lateinit var tvScreenTitle: TextView
    private lateinit var etAmount: EditText
    private lateinit var etTitle: EditText
    private lateinit var etNotes: EditText
    private lateinit var tvDate: TextView
    private lateinit var cbExcludeFromBudget: CheckBox

    private lateinit var btnExpense: Button
    private lateinit var btnIncome: Button
    private lateinit var btnSaveTransaction: Button

    private lateinit var tvSelectedCategory: TextView
    private lateinit var rvCategories: RecyclerView

    private var transactionType = "expense"
    private var selectedCategory = ""
    private var selectedDate = ""
    private var selectedTimestamp = ""

    private var isEditMode = false
    private var documentIdToUpdate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Find views
        tvScreenTitle = findViewById(R.id.tvAddTransactionTitle)
        etAmount = findViewById(R.id.etAmount)
        etTitle = findViewById(R.id.etTitle)
        etNotes = findViewById(R.id.etNotes)
        tvDate = findViewById(R.id.tvDate)
        cbExcludeFromBudget = findViewById(R.id.cbExcludeFromBudget)

        btnExpense = findViewById(R.id.btnExpense)
        btnIncome = findViewById(R.id.btnIncome)
        btnSaveTransaction = findViewById(R.id.btnSaveTransaction)

        tvSelectedCategory = findViewById(R.id.tvSelectedCategory)
        rvCategories = findViewById(R.id.rvCategories)

        // Setup category RecyclerView
        setupCategories()

        // Type button click listeners
        btnExpense.setOnClickListener {
            selectExpense()
        }

        btnIncome.setOnClickListener {
            selectIncome()
        }

        // Date click listener
        tvDate.setOnClickListener {
            showDatePicker()
        }

        // Apply theme
        applyTheme(ThemeManager.getTheme(this))

        // Default state
        selectExpense()

        // Check if editing existing transaction
        isEditMode = intent.getBooleanExtra("isEditMode", false)
        if (isEditMode) {
            documentIdToUpdate = intent.getStringExtra("documentId") ?: ""
            val amount = intent.getDoubleExtra("amount", 0.0)
            val title = intent.getStringExtra("title") ?: ""
            val notes = intent.getStringExtra("notes") ?: ""
            val category = intent.getStringExtra("category") ?: ""
            val type = intent.getStringExtra("type") ?: "expense"
            val date = intent.getStringExtra("date") ?: ""
            val timestamp = intent.getStringExtra("timestamp") ?: ""
            val excluded = intent.getBooleanExtra("excludedFromBudget", false)

            tvScreenTitle.text = "Edit Transaction"
            btnSaveTransaction.text = "UPDATE TRANSACTION"

            if (amount > 0) {
                val formattedAmount = if (amount % 1.0 == 0.0) {
                    amount.toLong().toString()
                } else {
                    amount.toBigDecimal().toPlainString()
                }
                etAmount.setText(formattedAmount)
                etAmount.setSelection(etAmount.text.length)
            }

            etTitle.setText(title)
            etNotes.setText(notes)
            cbExcludeFromBudget.isChecked = excluded

            if (category.isNotEmpty()) {
                selectedCategory = category
                tvSelectedCategory.text = "Category: $category"
            }

            if (type.equals("income", ignoreCase = true)) {
                selectIncome()
            } else {
                selectExpense()
            }

            selectedDate = date
            selectedTimestamp = timestamp
            if (selectedDate.isNotEmpty()) {
                tvDate.text = "Date: $selectedDate"
            }
        }

        // Save / Update button
        btnSaveTransaction.setOnClickListener {
            saveTransaction()
        }
    }

    private fun applyTheme(theme: AppTheme) {
        findViewById<ScrollView>(R.id.addTransactionScrollView)?.setBackgroundColor(theme.bgDark)
        window.statusBarColor = theme.bgDark
        window.navigationBarColor = theme.cardDark

        tvScreenTitle.setTextColor(theme.textLight)
        tvDate.setTextColor(theme.textLight)
        tvSelectedCategory.setTextColor(theme.lightShade)
        cbExcludeFromBudget.buttonTintList = ColorStateList.valueOf(theme.lightShade)
        cbExcludeFromBudget.setTextColor(theme.textLight)
        btnSaveTransaction.backgroundTintList = ColorStateList.valueOf(theme.buttonShade)
        btnSaveTransaction.setTextColor(theme.textLight)

        if (transactionType == "income") {
            selectIncome()
        } else {
            selectExpense()
        }
    }

    private fun selectExpense() {
        val theme = ThemeManager.getTheme(this)
        transactionType = "expense"
        btnExpense.backgroundTintList =
            ColorStateList.valueOf(theme.lightShade)
        btnExpense.setTextColor(theme.textDark)

        btnIncome.backgroundTintList =
            ColorStateList.valueOf(theme.buttonShade)
        btnIncome.setTextColor(theme.textLight)
    }

    private fun selectIncome() {
        val theme = ThemeManager.getTheme(this)
        transactionType = "income"
        btnIncome.backgroundTintList =
            ColorStateList.valueOf(theme.lightShade)
        btnIncome.setTextColor(theme.textDark)

        btnExpense.backgroundTintList =
            ColorStateList.valueOf(theme.buttonShade)
        btnExpense.setTextColor(theme.textLight)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        if (selectedDate.isNotEmpty()) {
            try {
                val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate)
                if (parsed != null) {
                    calendar.time = parsed
                }
            } catch (_: Exception) {}
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog =
            DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val cal = Calendar.getInstance()
                    cal.set(selectedYear, selectedMonth, selectedDay)
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    selectedDate = dateFormat.format(cal.time)
                    tvDate.text = "Date: $selectedDate"
                },
                year,
                month,
                day
            )

        datePickerDialog.show()
    }

    private fun setupCategories() {
        val categories =
            mutableListOf(
                Category("Food", "🍔"),
                Category("Groceries", "🛒"),
                Category("Transport", "🚕"),
                Category("Shopping", "🛍️"),
                Category("Medical", "💊"),
                Category("Education", "📚"),
                Category("Bills", "🧾"),
                Category("Subscriptions", "📺"),
                Category("Entertainment", "🎬"),
                Category("Travel", "✈️"),
                Category("Personal", "👤"),
                Category("Miscellaneous", "📦")
            )

        val adapter = CategoryAdapter(categories) { category ->
            selectedCategory = category.name
            tvSelectedCategory.text = "Category: ${category.name}"
            Toast.makeText(this, "${category.name} selected", Toast.LENGTH_SHORT).show()
        }

        rvCategories.layoutManager = GridLayoutManager(this, 4)
        rvCategories.adapter = adapter

        db.collection("customCategories")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val name = document.getString("name") ?: continue
                    val icon = document.getString("icon") ?: continue
                    categories.add(Category(name, icon))
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    this,
                    "Failed to load custom categories: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun buildTimestampForDate(targetDate: String, oldTimestamp: String, docId: String): String {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        var timePart: String? = null

        if (oldTimestamp.isNotEmpty()) {
            try {
                val parsedOld = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).parse(oldTimestamp)
                if (parsedOld != null) {
                    timePart = timeFormat.format(parsedOld)
                }
            } catch (_: Exception) {}
        }

        if (timePart == null && docId.isNotEmpty()) {
            val parts = docId.split("__")
            if (parts.size >= 3) {
                timePart = parts[2]
            }
        }

        if (timePart == null) {
            timePart = timeFormat.format(Date())
        }

        try {
            val targetCal = Calendar.getInstance()
            val parsedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(targetDate)
            if (parsedDate != null) {
                targetCal.time = parsedDate
            }
            val timeCal = Calendar.getInstance()
            val parsedTime = timeFormat.parse(timePart)
            if (parsedTime != null) {
                timeCal.time = parsedTime
                targetCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                targetCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
            }
            val outFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            return outFormat.format(targetCal.time)
        } catch (_: Exception) {
            return targetDate
        }
    }

    private fun formatAmount(amount: Double): String {
        return if (amount % 1.0 == 0.0) {
            amount.toLong().toString()
        } else {
            String.format(Locale.getDefault(), "%.2f", amount)
        }
    }

    private fun saveTransaction() {
        val amountText = etAmount.text.toString().trim()
        val title = etTitle.text.toString().trim()
        val notes = etNotes.text.toString().trim()
        val isExcluded = cbExcludeFromBudget.isChecked

        if (amountText.isEmpty()) {
            etAmount.error = "Enter amount"
            return
        }

        if (title.isEmpty()) {
            etTitle.error = "Enter title"
            return
        }

        if (selectedCategory.isEmpty()) {
            Toast.makeText(this, "Select a category", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            etAmount.error = "Enter a valid amount"
            return
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        val finalDate = if (selectedDate.isNotEmpty()) selectedDate else currentDate

        val finalTimestamp = if (isEditMode) {
            buildTimestampForDate(finalDate, selectedTimestamp, documentIdToUpdate)
        } else {
            val timestampFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            timestampFormat.format(Date())
        }

        val transaction = Transaction(
            amount = amount,
            title = title,
            notes = notes,
            category = selectedCategory,
            type = transactionType,
            date = finalDate,
            timestamp = finalTimestamp,
            excludedFromBudget = isExcluded
        )

        checkBudgetAndSave(transaction)
    }

    private fun checkBudgetAndSave(transaction: Transaction) {
        if (transaction.type != "expense" || transaction.excludedFromBudget) {
            executeSave(transaction)
            return
        }

        btnSaveTransaction.isEnabled = false

        db.collection("settings").document("budget").get()
            .addOnSuccessListener { budgetDoc ->
                val monthlyBudget = budgetDoc.getDouble("monthlyBudget") ?: 3000.0
                val targetMonth = if (transaction.date.length >= 7) {
                    transaction.date.substring(0, 7)
                } else {
                    SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
                }

                db.collection("transactions").get()
                    .addOnSuccessListener { result ->
                        btnSaveTransaction.isEnabled = true
                        var existingSpent = 0.0
                        for (doc in result) {
                            if (isEditMode && doc.id == documentIdToUpdate) {
                                continue
                            }
                            val tx = doc.toObject(Transaction::class.java)
                            if (tx.type == "expense" && tx.date.startsWith(targetMonth) && !tx.excludedFromBudget) {
                                existingSpent += tx.amount
                            }
                        }

                        val newTotalSpent = existingSpent + transaction.amount
                        if (newTotalSpent > monthlyBudget) {
                            showBudgetExceededDialog(
                                monthlyBudget = monthlyBudget,
                                currentSpent = existingSpent,
                                newTotalSpent = newTotalSpent,
                                transaction = transaction
                            )
                        } else {
                            executeSave(transaction)
                        }
                    }
                    .addOnFailureListener {
                        btnSaveTransaction.isEnabled = true
                        executeSave(transaction)
                    }
            }
            .addOnFailureListener {
                btnSaveTransaction.isEnabled = true
                executeSave(transaction)
            }
    }

    private fun showBudgetExceededDialog(
        monthlyBudget: Double,
        currentSpent: Double,
        newTotalSpent: Double,
        transaction: Transaction
    ) {
        val theme = ThemeManager.getTheme(this)
        val exceededBy = newTotalSpent - monthlyBudget
        val message = "Note: This transaction will exceed your monthly budget!\n\n" +
                "• Monthly Budget: ₹${formatAmount(monthlyBudget)}\n" +
                "• Current Spent: ₹${formatAmount(currentSpent)}\n" +
                "• New Total: ₹${formatAmount(newTotalSpent)}\n" +
                "• Exceeds Budget by: ₹${formatAmount(exceededBy)}\n\n" +
                "Do you want to continue anyway?"

        val dialog = AlertDialog.Builder(this)
            .setTitle("⚠️ Budget Warning")
            .setMessage(message)
            .setPositiveButton("Continue Anyway") { _, _ ->
                executeSave(transaction)
            }
            .setNegativeButton("Cancel") { d, _ ->
                btnSaveTransaction.isEnabled = true
                d.dismiss()
            }
            .setCancelable(false)
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
    }

    private fun executeSave(transaction: Transaction) {
        btnSaveTransaction.isEnabled = false
        if (isEditMode) {
            if (documentIdToUpdate.isEmpty()) {
                btnSaveTransaction.isEnabled = true
                Toast.makeText(this, "Error: Missing document ID to update", Toast.LENGTH_SHORT).show()
                return
            }

            db.collection("transactions")
                .document(documentIdToUpdate)
                .set(transaction)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Transaction updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                .addOnFailureListener { error ->
                    btnSaveTransaction.isEnabled = true
                    Toast.makeText(
                        this,
                        "Failed to update: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        } else {
            val userName = "Akshat_Pandey"
            val documentDateFormat = SimpleDateFormat("yyyy-MM-dd__HH:mm", Locale.getDefault())
            val documentId = "${userName}__${documentDateFormat.format(Date())}"

            db.collection("transactions")
                .document(documentId)
                .set(transaction)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Transaction saved successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                .addOnFailureListener { error ->
                    btnSaveTransaction.isEnabled = true
                    Toast.makeText(
                        this,
                        "Failed to save: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }
}
