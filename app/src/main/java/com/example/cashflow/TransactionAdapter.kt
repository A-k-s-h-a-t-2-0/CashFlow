package com.example.cashflow

import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

sealed class TransactionListItem {
    data class MonthHeader(
        val monthKey: String,
        val monthDisplay: String,
        val totalExpense: Double,
        val totalIncome: Double,
        val count: Int,
        val totalExcludedExpense: Double = 0.0
    ) : TransactionListItem()

    data class Entry(
        val transaction: Transaction
    ) : TransactionListItem()
}

class TransactionAdapter(
    private var items: List<TransactionListItem>,
    private val onTransactionLongPressed: (Transaction) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_TRANSACTION = 1
    }

    class MonthHeaderViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val tvMonthTitle: TextView = itemView.findViewById(R.id.tvMonthTitle)
        val tvMonthCount: TextView = itemView.findViewById(R.id.tvMonthCount)
        val tvMonthSummary: TextView = itemView.findViewById(R.id.tvMonthSummary)
    }

    class TransactionViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvTransactionTitle)
        val details: TextView = itemView.findViewById(R.id.tvTransactionDetails)
        val notes: TextView = itemView.findViewById(R.id.tvTransactionNotes)
        val amount: TextView = itemView.findViewById(R.id.tvTransactionAmount)
        val excludedBadge: TextView = itemView.findViewById(R.id.tvExcludedBadge)
    }

    fun setItems(newItems: List<TransactionListItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is TransactionListItem.MonthHeader -> TYPE_HEADER
            is TransactionListItem.Entry -> TYPE_TRANSACTION
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            val view = inflater.inflate(R.layout.item_month_header, parent, false)
            MonthHeaderViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_transaction, parent, false)
            TransactionViewHolder(view)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val theme = ThemeManager.getTheme(holder.itemView.context)

        when (val item = items[position]) {
            is TransactionListItem.MonthHeader -> {
                val headerHolder = holder as MonthHeaderViewHolder
                val headerShape = android.graphics.drawable.GradientDrawable().apply {
                    setColor(theme.bgDark)
                    cornerRadius = 16f
                    setStroke(2, theme.buttonShade)
                }
                headerHolder.itemView.background = headerShape
                headerHolder.tvMonthTitle.setTextColor(theme.lightShade)
                headerHolder.tvMonthCount.setTextColor(theme.textLight)
                headerHolder.tvMonthSummary.setTextColor(theme.textLight)

                headerHolder.tvMonthTitle.text = item.monthDisplay
                headerHolder.tvMonthCount.text =
                    "${item.count} ${if (item.count == 1) "transaction" else "transactions"}"

                val expStr = "Expense: ₹${formatAmount(item.totalExpense)}"
                val incStr = "Income: ₹${formatAmount(item.totalIncome)}"
                val excludedNote = if (item.totalExcludedExpense > 0) {
                    " (₹${formatAmount(item.totalExcludedExpense)} excluded from budget)"
                } else ""
                headerHolder.tvMonthSummary.text = "$expStr$excludedNote • $incStr"
            }

            is TransactionListItem.Entry -> {
                val txHolder = holder as TransactionViewHolder
                val transaction = item.transaction

                txHolder.itemView.setBackgroundColor(theme.cardDark)
                txHolder.title.setTextColor(theme.textLight)
                txHolder.amount.setTextColor(theme.lightShade)

                txHolder.title.text = transaction.title

                if (transaction.excludedFromBudget) {
                    txHolder.excludedBadge.visibility = View.VISIBLE
                    txHolder.excludedBadge.setTextColor(Color.parseColor("#FFA726"))
                } else {
                    txHolder.excludedBadge.visibility = View.GONE
                }

                val detailsText = "${transaction.category} • ${transaction.date}"
                val spannable = SpannableString(detailsText)
                val dateIndex = detailsText.indexOf(transaction.date)
                if (dateIndex >= 0 && transaction.date.isNotEmpty()) {
                    spannable.setSpan(
                        StyleSpan(Typeface.BOLD),
                        dateIndex,
                        dateIndex + transaction.date.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                txHolder.details.text = spannable

                if (transaction.notes.isNotEmpty()) {
                    txHolder.notes.visibility = View.VISIBLE
                    txHolder.notes.text = transaction.notes
                } else {
                    txHolder.notes.visibility = View.GONE
                }

                val sign = if (transaction.type == "income") "+" else "-"
                txHolder.amount.text = "$sign ₹${formatAmount(transaction.amount)}"

                txHolder.itemView.setOnLongClickListener {
                    onTransactionLongPressed(transaction)
                    true
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    private fun formatAmount(amount: Double): String {
        return if (amount % 1.0 == 0.0) {
            amount.toLong().toString()
        } else {
            amount.toBigDecimal().toPlainString()
        }
    }
}
