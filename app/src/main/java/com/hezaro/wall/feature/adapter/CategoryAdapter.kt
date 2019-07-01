package com.hezaro.wall.feature.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hezaro.wall.R
import com.hezaro.wall.data.model.Category
import com.hezaro.wall.feature.adapter.holder.CategoryHolder

/**
 * @author ali (alirezaiyann@gmail.com)
 * @since 7/1/19 12:59 AM.
 */
class CategoryAdapter(private val categories: ArrayList<Category>) : RecyclerView.Adapter<CategoryHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = CategoryHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_category, parent, false
        )
    )

    override fun getItemCount() = categories.size

    override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
        holder.bind(categories[position])
    }
}