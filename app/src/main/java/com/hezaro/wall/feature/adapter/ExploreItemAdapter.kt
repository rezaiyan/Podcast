package com.hezaro.wall.feature.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hezaro.wall.R
import kotlinx.android.synthetic.main.item_explore_child.view.childTitle

/**
 * @author ali (alirezaiyann@gmail.com)
 * @since 6/25/19 11:51 AM.
 */

class ExploreItemAdapter(private val items: MutableList<String>) :
    RecyclerView.Adapter<ExploreItemAdapter.ItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_explore_child, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ExploreItemAdapter.ItemHolder, position: Int) = holder.bind(items[position])

    inner class ItemHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: String) {

            view.childTitle.text = item
        }
    }
}