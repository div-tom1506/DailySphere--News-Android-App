package com.divyanshu.dailysphere.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.divyanshu.dailysphere.R
import androidx.recyclerview.widget.RecyclerView
import com.divyanshu.dailysphere.model.CategoryItem

class CategoryAdapter(
    private val categories: List<CategoryItem>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var selectedPosition = 0

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.categoryName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = categories[position]
        holder.categoryName.text = item.name

        val currentPos = holder.adapterPosition
        if (currentPos == RecyclerView.NO_POSITION) return  // Safety check

        // Highlight selection
        if (selectedPosition == currentPos) {
            holder.categoryName.setBackgroundResource(R.drawable.category_chip_selected)
        } else {
            holder.categoryName.setBackgroundResource(R.drawable.category_chip_background)
        }

        holder.itemView.setOnClickListener {
            val clickedPos = holder.adapterPosition
            if (clickedPos == RecyclerView.NO_POSITION) return@setOnClickListener

            val previousSelected = selectedPosition
            selectedPosition = clickedPos
            notifyItemChanged(previousSelected)
            notifyItemChanged(clickedPos)

            // Trigger the callback or action for the selected category
            onItemClick(categories[clickedPos].name) // Call the lambda function passed to the constructor
        }
    }



    override fun getItemCount(): Int = categories.size
}

