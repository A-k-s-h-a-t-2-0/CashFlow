package com.example.cashflow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(
    private val categories: List<Category>,
    private val onCategorySelected: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val icon: TextView =
            itemView.findViewById(R.id.tvCategoryIcon)

        val name: TextView =
            itemView.findViewById(R.id.tvCategoryName)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_category,
                parent,
                false
            )

        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: CategoryViewHolder,
        position: Int
    ) {

        val category = categories[position]

        holder.icon.text = category.icon
        holder.name.text = category.name

        holder.itemView.setOnClickListener {
            onCategorySelected(category)
        }
    }

    override fun getItemCount(): Int {
        return categories.size
    }
}