package com.example.cashflow

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    private lateinit var btnHome: Button
    private lateinit var btnTransactions: Button
    private lateinit var btnBudgets: Button
    private lateinit var btnMore: Button

    private val selectedColor =
        Color.parseColor("#A5D6A7")

    private val unselectedColor =
        Color.parseColor("#365A3A")

    private val selectedTextColor =
        Color.parseColor("#16301A")

    private val unselectedTextColor =
        Color.parseColor("#E8F5E9")

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_main
        )

        btnHome =
            findViewById(R.id.btnHome)

        btnTransactions =
            findViewById(R.id.btnTransactions)

        btnBudgets =
            findViewById(R.id.btnBudgets)

        btnMore =
            findViewById(R.id.btnMore)

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