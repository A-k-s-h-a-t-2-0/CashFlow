package com.example.cashflow

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class MoreFragment : Fragment(R.layout.fragment_more) {

    private lateinit var btnSettings: Button
    private lateinit var btnCategories: Button
    private lateinit var btnSpendingSummary: Button

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        btnCategories =
            view.findViewById(R.id.btnCategories)

        btnSpendingSummary =
            view.findViewById(R.id.btnSpendingSummary)

        btnSettings =
            view.findViewById(R.id.btnSettings)

        applyTheme(ThemeManager.getTheme(requireContext()))

        // Categories button
        btnCategories.setOnClickListener {

            parentFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    CategoriesFragment()
                )
                .addToBackStack(null)
                .commit()
        }

        // Spending Summary button
        btnSpendingSummary.setOnClickListener {

            parentFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    SpendingSummaryFragment()
                )
                .addToBackStack(null)
                .commit()
        }

        // Settings button
        btnSettings.setOnClickListener {

            parentFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    SettingsFragment()
                )
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isAdded) {
            applyTheme(ThemeManager.getTheme(requireContext()))
        }
    }

    private fun applyTheme(theme: AppTheme) {
        view?.findViewById<View>(R.id.moreRootLayout)?.setBackgroundColor(theme.bgDark)
        view?.findViewById<TextView>(R.id.tvMoreHeading)?.setTextColor(theme.textLight)

        val buttons = listOf(btnSettings, btnCategories, btnSpendingSummary)
        for (btn in buttons) {
            btn.backgroundTintList = ColorStateList.valueOf(theme.buttonShade)
            btn.setTextColor(theme.textLight)
        }
    }
}
