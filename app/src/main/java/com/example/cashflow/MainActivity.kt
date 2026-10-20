package com.example.cashflow

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    private lateinit var mainRootLayout: LinearLayout
    private lateinit var bottomNavLayout: LinearLayout

    private lateinit var btnHome: Button
    private lateinit var btnTransactions: Button
    private lateinit var btnBudgets: Button
    private lateinit var btnMore: Button

    private var currentSelectedButton: Button? = null

    private var selectedColor = 0
    private var unselectedColor = 0
    private var selectedTextColor = 0
    private var unselectedTextColor = 0

    private val themeListener: (AppTheme) -> Unit = { theme ->
        applyTheme(theme)
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_main
        )

        mainRootLayout =
            findViewById(R.id.mainRootLayout)

        bottomNavLayout =
            findViewById(R.id.bottomNavLayout)

        btnHome =
            findViewById(R.id.btnHome)

        btnTransactions =
            findViewById(R.id.btnTransactions)

        btnBudgets =
            findViewById(R.id.btnBudgets)

        btnMore =
            findViewById(R.id.btnMore)

        ThemeManager.addListener(themeListener)
        applyTheme(ThemeManager.getTheme(this))

        if (savedInstanceState == null) {

            loadFragment(
                HomeFragment()
            )

            selectButton(
                btnHome
            )
        }

        btnHome.setOnClickListener {

            loadFragment(
                HomeFragment()
            )

            selectButton(
                btnHome
            )
        }

        btnTransactions.setOnClickListener {

            loadFragment(
                TransactionsFragment()
            )

            selectButton(
                btnTransactions
            )
        }

        btnBudgets.setOnClickListener {

            loadFragment(
                BudgetsFragment()
            )

            selectButton(
                btnBudgets
            )
        }

        btnMore.setOnClickListener {

            loadFragment(
                MoreFragment()
            )

            selectButton(
                btnMore
            )
        }
    }

    override fun onResume() {
        super.onResume()
        applyTheme(ThemeManager.getTheme(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        ThemeManager.removeListener(themeListener)
    }

    private fun applyTheme(theme: AppTheme) {
        mainRootLayout.setBackgroundColor(theme.bgDark)
        bottomNavLayout.setBackgroundColor(theme.cardDark)

        window.statusBarColor = theme.bgDark
        window.navigationBarColor = theme.cardDark

        selectedColor = theme.lightShade
        unselectedColor = theme.buttonShade
        selectedTextColor = theme.textDark
        unselectedTextColor = theme.textLight

        currentSelectedButton?.let {
            selectButton(it)
        }
    }

    private fun loadFragment(
        fragment: Fragment
    ) {

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragmentContainer,
                fragment
            )
            .commit()
    }

    private fun selectButton(
        selectedButton: Button
    ) {
        currentSelectedButton = selectedButton

        val buttons =
            listOf(
                btnHome,
                btnTransactions,
                btnBudgets,
                btnMore
            )

        for (button in buttons) {

            if (button == selectedButton) {

                button.backgroundTintList =
                    ColorStateList.valueOf(
                        selectedColor
                    )

                button.setTextColor(
                    selectedTextColor
                )

            } else {

                button.backgroundTintList =
                    ColorStateList.valueOf(
                        unselectedColor
                    )

                button.setTextColor(
                    unselectedTextColor
                )
            }
        }
    }
}
