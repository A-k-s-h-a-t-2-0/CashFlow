package com.example.cashflow

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment

class MoreFragment : Fragment(R.layout.fragment_more) {

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val btnCategories =
            view.findViewById<Button>(R.id.btnCategories)

        val btnSpendingSummary =
            view.findViewById<Button>(R.id.btnSpendingSummary)

        // Settings button
        val btnSettings =
            view.findViewById<Button>(R.id.btnSettings)

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
}