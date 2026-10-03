package com.example.cashflow

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    private lateinit var etAmount: EditText
    private lateinit var etTitle: EditText
    private lateinit var etNotes: EditText

    private lateinit var btnExpense: Button
    private lateinit var btnIncome: Button
    private lateinit var btnSaveTransaction: Button

    private lateinit var tvSelectedCategory: TextView
    private lateinit var rvCategories: RecyclerView

    private var transactionType = "expense"
    private var selectedCategory = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_transaction)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Find views
        etAmount = findViewById(R.id.etAmount)
        etTitle = findViewById(R.id.etTitle)
        etNotes = findViewById(R.id.etNotes)

        btnExpense = findViewById(R.id.btnExpense)
        btnIncome = findViewById(R.id.btnIncome)
        btnSaveTransaction =
            findViewById(R.id.btnSaveTransaction)

        tvSelectedCategory =
            findViewById(R.id.tvSelectedCategory)

        rvCategories =
            findViewById(R.id.rvCategories)

        // Setup category RecyclerView
        setupCategories()
        btnExpense.backgroundTintList =
            android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#A5D6A7")
            )

        btnExpense.setTextColor(
            android.graphics.Color.parseColor("#16301A")
        )

        // Expense button
        btnExpense.setOnClickListener {

            transactionType = "expense"

            btnExpense.backgroundTintList =
                android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#A5D6A7")
                )

            btnExpense.setTextColor(
                android.graphics.Color.parseColor("#16301A")
            )

            btnIncome.backgroundTintList =
                android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#365A3A")
                )

            btnIncome.setTextColor(
                android.graphics.Color.parseColor("#E8F5E9")
            )
        }

        btnIncome.setOnClickListener {

            transactionType = "income"

            btnIncome.backgroundTintList =
                android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#A5D6A7")
                )

            btnIncome.setTextColor(
                android.graphics.Color.parseColor("#16301A")
            )

            btnExpense.backgroundTintList =
                android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#365A3A")
                )

            btnExpense.setTextColor(
                android.graphics.Color.parseColor("#E8F5E9")
            )
        }

        // Save button
        btnSaveTransaction.setOnClickListener {
            saveTransaction()
        }
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

        val adapter =
            CategoryAdapter(
                categories
            ) { category ->

                selectedCategory =
                    category.name

                tvSelectedCategory.text =
                    "Category: ${category.name}"

                Toast.makeText(
                    this,
                    "${category.name} selected",
                    Toast.LENGTH_SHORT
                ).show()
            }

        rvCategories.layoutManager =
            GridLayoutManager(
                this,
                4
            )

        rvCategories.adapter =
            adapter

        db.collection("customCategories")
            .get()
            .addOnSuccessListener { result ->

                for (document in result) {

                    val name =
                        document.getString(
                            "name"
                        ) ?: continue

                    val icon =
                        document.getString(
                            "icon"
                        ) ?: continue

                    categories.add(
                        Category(
                            name,
                            icon
                        )
                    )
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

    private fun saveTransaction() {

        // Get user input
        val amountText =
            etAmount.text.toString().trim()

        val title =
            etTitle.text.toString().trim()

        val notes =
            etNotes.text.toString().trim()

        // Validate amount
        if (amountText.isEmpty()) {
            etAmount.error = "Enter amount"
            return
        }

        // Validate title
        if (title.isEmpty()) {
            etTitle.error = "Enter title"
            return
        }

        // Validate category
        if (selectedCategory.isEmpty()) {

            Toast.makeText(
                this,
                "Select a category",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        // Convert amount to Double
        val amount =
            amountText.toDoubleOrNull()

        if (amount == null || amount <= 0) {

            etAmount.error =
                "Enter a valid amount"

            return
        }

        // Current date
        val dateFormat =
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            )

        val currentDate =
            dateFormat.format(Date())

        val timestampFormat =
            SimpleDateFormat(
                "dd MMM yyyy, HH:mm",
                Locale.getDefault()
            )

        val currentTimestamp =
            timestampFormat.format(Date())

        // Create Transaction object
        val transaction = Transaction(
            amount = amount,
            title = title,
            notes = notes,
            category = selectedCategory,
            type = transactionType,
            date = currentDate,
            timestamp = currentTimestamp
        )

        // ------------------------------------------------
        // CREATE CUSTOM FIRESTORE DOCUMENT ID
        // Format:
        // Akshat_Pandey__2026-10-02__22:30
        // ------------------------------------------------

        val userName = "Akshat_Pandey"

        val documentDateFormat =
            SimpleDateFormat(
                "yyyy-MM-dd__HH:mm",
                Locale.getDefault()
            )

        val documentId =
            "${userName}__${documentDateFormat.format(Date())}"

        // Save transaction to Firestore
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

                Toast.makeText(
                    this,
                    "Failed to save: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}