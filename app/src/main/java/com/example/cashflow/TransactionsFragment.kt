package com.example.cashflow

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionsFragment :
    Fragment(R.layout.fragment_transactions) {

    private lateinit var db: FirebaseFirestore
    private lateinit var rvTransactions: RecyclerView

    private val transactions =
        mutableListOf<Transaction>()

    private lateinit var adapter: TransactionAdapter

    private val timestampFormat =
        SimpleDateFormat(
            "dd MMM yyyy, HH:mm",
            Locale.getDefault()
        )

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        db = FirebaseFirestore.getInstance()

        rvTransactions =
            view.findViewById(
                R.id.rvTransactions
            )

        adapter =
            TransactionAdapter(
                transactions
            ) { transaction ->

                showDeleteDialog(transaction)
            }

        rvTransactions.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        rvTransactions.adapter =
            adapter

        loadTransactions()
    }

    private fun loadTransactions() {

        db.collection("transactions")
            .get()
            .addOnSuccessListener { result ->

                transactions.clear()

                for (document in result) {

                    val transaction =
                        document.toObject(
                            Transaction::class.java
                        )

                    transaction.documentId =
                        document.id

                    transactions.add(transaction)
                }

                // Newest → oldest
                transactions.sortWith(
                    compareByDescending<Transaction> {

                        try {
                            timestampFormat.parse(
                                it.timestamp
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                )

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { error ->

                Toast.makeText(
                    requireContext(),
                    "Failed to load transactions: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun showDeleteDialog(
        transaction: Transaction
    ) {

        AlertDialog.Builder(
            requireContext()
        )
            .setTitle("Delete Transaction")
            .setMessage(
                "Are you sure you want to delete \"${transaction.title}\"?"
            )
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton(
                "Delete"
            ) { _, _ ->

                deleteTransaction(
                    transaction
                )
            }
            .show()
    }

    private fun deleteTransaction(
        transaction: Transaction
    ) {

        db.collection("transactions")
            .document(transaction.documentId)
            .delete()
            .addOnSuccessListener {

                transactions.remove(
                    transaction
                )

                adapter.notifyDataSetChanged()

                Toast.makeText(
                    requireContext(),
                    "Transaction deleted",
                    Toast.LENGTH_SHORT
                ).show()
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