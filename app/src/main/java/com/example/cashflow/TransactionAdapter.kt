package com.example.cashflow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(
    private val transactions: List<Transaction>,
    private val onTransactionLongPressed: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val title: TextView =
            itemView.findViewById(R.id.tvTransactionTitle)

        val details: TextView =
            itemView.findViewById(R.id.tvTransactionDetails)

        val notes: TextView =
            itemView.findViewById(R.id.tvTransactionNotes)

        val amount: TextView =
            itemView.findViewById(R.id.tvTransactionAmount)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransactionViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_transaction,
                parent,
                false
            )

        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: TransactionViewHolder,
        position: Int
    ) {

        val transaction = transactions[position]

        holder.title.text =
            transaction.title

        holder.details.text =
            "${transaction.category} • ${transaction.date}"

        holder.notes.text =
            transaction.notes

        val sign =
            if (transaction.type == "income") {
                "+"
            } else {
                "-"
            }

        holder.amount.text =
            "$sign ₹${formatAmount(transaction.amount)}"

        // Long press to delete
        holder.itemView.setOnLongClickListener {

            onTransactionLongPressed(transaction)

            true
        }
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    private fun formatAmount(
        amount: Double
    ): String {

        return if (amount % 1.0 == 0.0) {

            amount.toLong().toString()

        } else {

            amount.toBigDecimal()
                .toPlainString()
        }
    }
}