package com.hezaro.wall.feature.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.hezaro.wall.R
import com.hezaro.wall.data.model.ExploreItem.Best
import com.hezaro.wall.data.model.ExploreItem.Category
import com.hezaro.wall.data.model.ExploreItem.Last
import com.hezaro.wall.data.model.ExploreItem.Recommended
import com.hezaro.wall.feature.adapter.holder.ExploreHolder

/**
 * @author ali (alirezaiyann@gmail.com)
 * @since 6/25/19 11:41 AM.
 */
class ExploreAdapter(
    private val best: Best,
    private val category: Category,
    private val last: Last,
    private val recommended: Recommended
) :
    RecyclerView.Adapter<ViewHolder>() {

    private val viewPool = RecyclerView.RecycledViewPool()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        0 -> ExploreHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_explore, parent, false),
            viewPool
            , { e, i -> }, { p, i -> })
        1 -> ExploreHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_explore, parent, false),
            viewPool
            , { e, i -> }, { p, i -> })
        2 -> ExploreHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_explore, parent, false),
            viewPool
            , { e, i -> }, { p, i -> })
        else -> ExploreHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_explore, parent, false),
            viewPool
            , { e, i -> }, { p, i -> })

    }

    override fun getItemViewType(position: Int) = position

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (position) {
            0 -> (holder as ExploreHolder).bindBest(best)
            1 -> (holder as ExploreHolder).bindLast(last)
            2 -> (holder as ExploreHolder).bindBanner(recommended)
            3 -> (holder as ExploreHolder).bindCategory(category)
        }
    }

    override fun getItemCount() = 4
}