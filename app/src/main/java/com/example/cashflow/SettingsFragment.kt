package com.example.cashflow

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var db: FirebaseFirestore

    private lateinit var scrollView: ScrollView
    private lateinit var tvSettingsTitle: TextView
    private lateinit var tvSettingsName: TextView
    private lateinit var tvSettingsCurrency: TextView
    private lateinit var tvSettingsDatabase: TextView

    private lateinit var btnOptionManageBudget: Button
    private lateinit var btnOptionUpdateBalance: Button
    private lateinit var btnOptionThemes: Button

    private lateinit var layoutManageBudget: LinearLayout
    private lateinit var tvManageBudgetTitle: TextView
    private lateinit var tvCurrentBudget: TextView
    private lateinit var etMonthlyBudget: EditText
    private lateinit var btnSaveBudget: Button

    private lateinit var layoutUpdateBalance: LinearLayout
    private lateinit var tvUpdateBalanceTitle: TextView
    private lateinit var tvCurrentBalanceSettings: TextView
    private lateinit var etNewBalance: EditText
    private lateinit var btnSaveBalance: Button

    private lateinit var layoutThemes: LinearLayout
    private lateinit var tvThemesHeading: TextView
    private lateinit var rgThemes: RadioGroup

    private lateinit var rbViolet: RadioButton
    private lateinit var rbIndigo: RadioButton
    private lateinit var rbBlue: RadioButton
    private lateinit var rbGreen: RadioButton
    private lateinit var rbYellow: RadioButton
    private lateinit var rbOrange: RadioButton
    private lateinit var rbRed: RadioButton

    private val defaultBudget = 3000.0
    private var initialBalance = 0.0
    private var netTransactions = 0.0
    private var currentBalance = 0.0
    private var isUpdatingRadioFromCode = false

    private val themeListener: (AppTheme) -> Unit = { theme ->
        applyTheme(theme)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        db = FirebaseFirestore.getInstance()

        scrollView = view.findViewById(R.id.settingsScrollView)
        tvSettingsTitle = view.findViewById(R.id.tvSettingsTitle)
        tvSettingsName = view.findViewById(R.id.tvSettingsName)
        tvSettingsCurrency = view.findViewById(R.id.tvSettingsCurrency)
        tvSettingsDatabase = view.findViewById(R.id.tvSettingsDatabase)

        btnOptionManageBudget = view.findViewById(R.id.btnOptionManageBudget)
        btnOptionUpdateBalance = view.findViewById(R.id.btnOptionUpdateBalance)
        btnOptionThemes = view.findViewById(R.id.btnOptionThemes)

        layoutManageBudget = view.findViewById(R.id.layoutManageBudget)
        tvManageBudgetTitle = view.findViewById(R.id.tvManageBudgetTitle)
        tvCurrentBudget = view.findViewById(R.id.tvCurrentBudget)
        etMonthlyBudget = view.findViewById(R.id.etMonthlyBudget)
        btnSaveBudget = view.findViewById(R.id.btnSaveBudget)

        layoutUpdateBalance = view.findViewById(R.id.layoutUpdateBalance)
        tvUpdateBalanceTitle = view.findViewById(R.id.tvUpdateBalanceTitle)
        tvCurrentBalanceSettings = view.findViewById(R.id.tvCurrentBalanceSettings)
        etNewBalance = view.findViewById(R.id.etNewBalance)
        btnSaveBalance = view.findViewById(R.id.btnSaveBalance)

        layoutThemes = view.findViewById(R.id.layoutThemes)
        tvThemesHeading = view.findViewById(R.id.tvThemesHeading)
        rgThemes = view.findViewById(R.id.rgThemes)

        rbViolet = view.findViewById(R.id.rbViolet)
        rbIndigo = view.findViewById(R.id.rbIndigo)
        rbBlue = view.findViewById(R.id.rbBlue)
        rbGreen = view.findViewById(R.id.rbGreen)
        rbYellow = view.findViewById(R.id.rbYellow)
        rbOrange = view.findViewById(R.id.rbOrange)
        rbRed = view.findViewById(R.id.rbRed)

        // Color-tint the radio buttons according to their VIBGYOR colors
        setupRadioButtonsTint()

        // Setup theme listener
        ThemeManager.addListener(themeListener)

        // Apply current theme
        val currentTheme = ThemeManager.getTheme(requireContext())
        applyTheme(currentTheme)
        selectCurrentThemeRadio(currentTheme.key)

        // Option 1: Manage Budget click listener
        btnOptionManageBudget.setOnClickListener {
            if (layoutManageBudget.visibility == View.VISIBLE) {
                layoutManageBudget.visibility = View.GONE
                updateOptionButtonStyles(null)
            } else {
                layoutManageBudget.visibility = View.VISIBLE
                layoutUpdateBalance.visibility = View.GONE
                layoutThemes.visibility = View.GONE
                updateOptionButtonStyles(btnOptionManageBudget)
            }
        }

        // Option 2: Update Balance click listener
        btnOptionUpdateBalance.setOnClickListener {
            if (layoutUpdateBalance.visibility == View.VISIBLE) {
                layoutUpdateBalance.visibility = View.GONE
                updateOptionButtonStyles(null)
            } else {
                layoutUpdateBalance.visibility = View.VISIBLE
                layoutManageBudget.visibility = View.GONE
                layoutThemes.visibility = View.GONE
                updateOptionButtonStyles(btnOptionUpdateBalance)
                loadBalance()
            }
        }

        // Option 3: Themes click listener
        btnOptionThemes.setOnClickListener {
            if (layoutThemes.visibility == View.VISIBLE) {
                layoutThemes.visibility = View.GONE
                updateOptionButtonStyles(null)
            } else {
                layoutThemes.visibility = View.VISIBLE
                layoutManageBudget.visibility = View.GONE
                layoutUpdateBalance.visibility = View.GONE
                updateOptionButtonStyles(btnOptionThemes)
            }
        }

        // Setup RadioGroup selection listener
        rgThemes.setOnCheckedChangeListener { _, checkedId ->
            if (isUpdatingRadioFromCode) return@setOnCheckedChangeListener

            val selectedKey = when (checkedId) {
                R.id.rbViolet -> "VIOLET"
                R.id.rbIndigo -> "INDIGO"
                R.id.rbBlue -> "BLUE"
                R.id.rbGreen -> "GREEN"
                R.id.rbYellow -> "YELLOW"
                R.id.rbOrange -> "ORANGE"
                R.id.rbRed -> "RED"
                else -> "GREEN"
            }

            ThemeManager.setSelectedTheme(requireContext(), selectedKey)
            val updatedTheme = ThemeManager.getTheme(requireContext())
            Toast.makeText(
                requireContext(),
                "${updatedTheme.name} theme applied",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnSaveBudget.setOnClickListener {
            saveBudget()
        }

        btnSaveBalance.setOnClickListener {
            saveBalance()
        }

        loadBudget()
        loadBalance()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ThemeManager.removeListener(themeListener)
    }

    private fun setupRadioButtonsTint() {
        val themeMap = mapOf(
            rbViolet to ThemeManager.THEMES[0].radioColor,
            rbIndigo to ThemeManager.THEMES[1].radioColor,
            rbBlue to ThemeManager.THEMES[2].radioColor,
            rbGreen to ThemeManager.THEMES[3].radioColor,
            rbYellow to ThemeManager.THEMES[4].radioColor,
            rbOrange to ThemeManager.THEMES[5].radioColor,
            rbRed to ThemeManager.THEMES[6].radioColor
        )

        for ((radioButton, color) in themeMap) {
            radioButton.buttonTintList = ColorStateList.valueOf(color)
        }
    }

    private fun selectCurrentThemeRadio(themeKey: String) {
        isUpdatingRadioFromCode = true
        val targetId = when (themeKey) {
            "VIOLET" -> R.id.rbViolet
            "INDIGO" -> R.id.rbIndigo
            "BLUE" -> R.id.rbBlue
            "GREEN" -> R.id.rbGreen
            "YELLOW" -> R.id.rbYellow
            "ORANGE" -> R.id.rbOrange
            "RED" -> R.id.rbRed
            else -> R.id.rbGreen
        }
        rgThemes.check(targetId)
        isUpdatingRadioFromCode = false
    }

    private fun updateOptionButtonStyles(activeButton: Button?) {
        val currentTheme = ThemeManager.getTheme(requireContext())
        val buttons = listOf(btnOptionManageBudget, btnOptionUpdateBalance, btnOptionThemes)

        for (btn in buttons) {
            if (btn == activeButton) {
                btn.backgroundTintList = ColorStateList.valueOf(currentTheme.lightShade)
                btn.setTextColor(currentTheme.textDark)
            } else {
                btn.backgroundTintList = ColorStateList.valueOf(currentTheme.buttonShade)
                btn.setTextColor(currentTheme.textLight)
            }
        }
    }

    private fun applyTheme(theme: AppTheme) {
        if (!isAdded) return

        scrollView.setBackgroundColor(theme.bgDark)

        tvSettingsTitle.setTextColor(theme.textLight)
        tvSettingsName.setTextColor(theme.textLight)
        tvSettingsCurrency.setTextColor(theme.lightShade)
        tvSettingsDatabase.setTextColor(theme.lightShade)

        val activeButton = when {
            layoutManageBudget.visibility == View.VISIBLE -> btnOptionManageBudget
            layoutUpdateBalance.visibility == View.VISIBLE -> btnOptionUpdateBalance
            layoutThemes.visibility == View.VISIBLE -> btnOptionThemes
            else -> null
        }
        updateOptionButtonStyles(activeButton)

        tvManageBudgetTitle.setTextColor(theme.textLight)
        tvCurrentBudget.setTextColor(theme.lightShade)
        btnSaveBudget.backgroundTintList = ColorStateList.valueOf(theme.buttonShade)
        btnSaveBudget.setTextColor(theme.textLight)

        tvUpdateBalanceTitle.setTextColor(theme.textLight)
        tvCurrentBalanceSettings.setTextColor(theme.lightShade)
        btnSaveBalance.backgroundTintList = ColorStateList.valueOf(theme.buttonShade)
        btnSaveBalance.setTextColor(theme.textLight)

        tvThemesHeading.setTextColor(theme.textLight)
        val radioButtons = listOf(rbViolet, rbIndigo, rbBlue, rbGreen, rbYellow, rbOrange, rbRed)
        for (rb in radioButtons) {
            rb.setTextColor(theme.textLight)
        }
    }

    private fun loadBudget() {
        db.collection("settings")
            .document("budget")
            .get()
            .addOnSuccessListener { document ->
                if (!isAdded) return@addOnSuccessListener
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
                if (!isAdded) return@addOnFailureListener
                Toast.makeText(
                    requireContext(),
                    "Failed to load budget: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
                showBudget(defaultBudget)
            }
    }

    private fun loadBalance() {
        db.collection("settings")
            .document("balance")
            .get()
            .addOnSuccessListener { balDoc ->
                if (!isAdded) return@addOnSuccessListener
                initialBalance = if (balDoc.exists()) {
                    balDoc.getDouble("initialBalance") ?: 0.0
                } else {
                    0.0
                }
                loadTransactionsForBalance()
            }
            .addOnFailureListener {
                if (!isAdded) return@addOnFailureListener
                initialBalance = 0.0
                loadTransactionsForBalance()
            }
    }

    private fun loadTransactionsForBalance() {
        db.collection("transactions")
            .get()
            .addOnSuccessListener { result ->
                if (!isAdded) return@addOnSuccessListener
                var totalIncome = 0.0
                var totalExpense = 0.0
                for (doc in result) {
                    val tx = doc.toObject(Transaction::class.java)
                    if (tx.type == "income") {
                        totalIncome += tx.amount
                    } else {
                        totalExpense += tx.amount
                    }
                }
                netTransactions = totalIncome - totalExpense
                currentBalance = initialBalance + netTransactions
                tvCurrentBalanceSettings.text = "Current Balance: ₹${formatAmount(currentBalance)}"
            }
            .addOnFailureListener {
                if (!isAdded) return@addOnFailureListener
                currentBalance = initialBalance
                tvCurrentBalanceSettings.text = "Current Balance: ₹${formatAmount(currentBalance)}"
            }
    }

    private fun saveBudget() {
        val budgetText = etMonthlyBudget.text.toString().trim()

        if (budgetText.isEmpty()) {
            etMonthlyBudget.error = "Enter monthly budget"
            return
        }

        val budget = budgetText.toDoubleOrNull()
        if (budget == null || budget <= 0) {
            etMonthlyBudget.error = "Enter a valid budget"
            return
        }

        val budgetData = hashMapOf(
            "monthlyBudget" to budget
        )

        db.collection("settings")
            .document("budget")
            .set(budgetData)
            .addOnSuccessListener {
                if (!isAdded) return@addOnSuccessListener
                showBudget(budget)
                etMonthlyBudget.text.clear()
                Toast.makeText(
                    requireContext(),
                    "Budget saved successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { error ->
                if (!isAdded) return@addOnFailureListener
                Toast.makeText(
                    requireContext(),
                    "Failed to save budget: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun saveBalance() {
        val balanceText = etNewBalance.text.toString().trim()

        if (balanceText.isEmpty()) {
            etNewBalance.error = "Enter balance amount"
            return
        }

        val targetBalance = balanceText.toDoubleOrNull()
        if (targetBalance == null) {
            etNewBalance.error = "Enter a valid amount"
            return
        }

        val updatedInitialBalance = targetBalance - netTransactions

        val balanceData = hashMapOf(
            "initialBalance" to updatedInitialBalance
        )

        db.collection("settings")
            .document("balance")
            .set(balanceData)
            .addOnSuccessListener {
                if (!isAdded) return@addOnSuccessListener
                initialBalance = updatedInitialBalance
                currentBalance = targetBalance
                tvCurrentBalanceSettings.text = "Current Balance: ₹${formatAmount(currentBalance)}"
                etNewBalance.text.clear()
                Toast.makeText(
                    requireContext(),
                    "Balance saved successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { error ->
                if (!isAdded) return@addOnFailureListener
                Toast.makeText(
                    requireContext(),
                    "Failed to save balance: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun showBudget(budget: Double) {
        tvCurrentBudget.text = "Current Budget: ₹${formatAmount(budget)}"
    }

    private fun formatAmount(amount: Double): String {
        return if (amount % 1.0 == 0.0) {
            amount.toLong().toString()
        } else {
            amount.toBigDecimal().toPlainString()
        }
    }
}
