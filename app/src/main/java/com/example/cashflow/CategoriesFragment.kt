package com.example.cashflow

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class CategoriesFragment :
    Fragment(R.layout.fragment_categories) {

    private lateinit var db: FirebaseFirestore
    private lateinit var rvCategories: RecyclerView

    private val categories =
        mutableListOf<Category>()

    private lateinit var adapter: CategoryAdapter

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        db = FirebaseFirestore.getInstance()

        rvCategories =
            view.findViewById(
                R.id.rvCategories
            )

        val btnAddCategory =
            view.findViewById<Button>(
                R.id.btnAddCategory
            )

        adapter =
            CategoryAdapter(
                categories
            ) { category ->
                // No action for now
            }

        rvCategories.layoutManager =
            GridLayoutManager(
                requireContext(),
                4
            )

        rvCategories.adapter =
            adapter

        btnAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }

        loadCategories()
    }

    private fun loadCategories() {

        categories.clear()

        // Default categories
        categories.add(
            Category("Food", "🍔")
        )

        categories.add(
            Category("Groceries", "🛒")
        )

        categories.add(
            Category("Transport", "🚕")
        )

        categories.add(
            Category("Shopping", "🛍️")
        )

        categories.add(
            Category("Medical", "💊")
        )

        categories.add(
            Category("Education", "📚")
        )

        categories.add(
            Category("Bills", "🧾")
        )

        categories.add(
            Category("Subscriptions", "📺")
        )

        categories.add(
            Category("Entertainment", "🎬")
        )

        categories.add(
            Category("Travel", "✈️")
        )

        categories.add(
            Category("Personal", "👤")
        )

        categories.add(
            Category("Miscellaneous", "📦")
        )

        // Custom categories
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
                    requireContext(),
                    "Failed to load custom categories: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()

                adapter.notifyDataSetChanged()
            }
    }

    private fun showAddCategoryDialog() {

        val layout =
            android.widget.LinearLayout(
                requireContext()
            )

        layout.orientation =
            android.widget.LinearLayout.VERTICAL

        layout.setPadding(
            50,
            10,
            50,
            0
        )

        val etCategoryName =
            EditText(requireContext())

        etCategoryName.hint =
            "Category name"

        etCategoryName.inputType =
            android.text.InputType.TYPE_CLASS_TEXT

        layout.addView(
            etCategoryName
        )

        val etCategoryEmoji =
            EditText(requireContext())

        etCategoryEmoji.hint =
            "Choose an emoji for the icon"

        etCategoryEmoji.inputType =
            android.text.InputType.TYPE_CLASS_TEXT

        layout.addView(
            etCategoryEmoji
        )

        val dialog =
            AlertDialog.Builder(
                requireContext()
            )
                .setTitle("Add Category")
                .setView(layout)
                .setNegativeButton(
                    "Cancel",
                    null
                )
                .setPositiveButton(
                    "Add",
                    null
                )
                .create()

        dialog.setOnShowListener {

            val addButton =
                dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
                )

            addButton.setOnClickListener {

                val categoryName =
                    etCategoryName.text
                        .toString()
                        .trim()

                val emoji =
                    etCategoryEmoji.text
                        .toString()
                        .trim()

                if (categoryName.isEmpty()) {

                    etCategoryName.error =
                        "Enter category name"

                    return@setOnClickListener
                }

                if (emoji.isEmpty()) {

                    etCategoryEmoji.error =
                        "Enter an emoji"

                    return@setOnClickListener
                }

                saveCustomCategory(
                    categoryName,
                    emoji,
                    dialog
                )
            }
        }

        dialog.show()
    }

    private fun saveCustomCategory(
        categoryName: String,
        emoji: String,
        dialog: AlertDialog
    ) {

        val categoryData =
            hashMapOf(
                "name" to categoryName,
                "icon" to emoji
            )

        db.collection("customCategories")
            .document(categoryName)
            .set(categoryData)
            .addOnSuccessListener {

                Toast.makeText(
                    requireContext(),
                    "Category added successfully",
                    Toast.LENGTH_SHORT
                ).show()

                dialog.dismiss()

                loadCategories()
            }
            .addOnFailureListener { error ->

                Toast.makeText(
                    requireContext(),
                    "Failed to add category: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}