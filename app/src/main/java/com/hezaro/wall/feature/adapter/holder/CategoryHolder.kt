package com.hezaro.wall.feature.adapter.holder

import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hezaro.wall.data.model.ExploreItem
import kotlinx.android.synthetic.main.item_explore.view.exploreItemRecyclerView
import kotlinx.android.synthetic.main.item_explore.view.exploreItemTitle

/**
 * @author ali (alirezaiyann@gmail.com)
 * @since 6/25/19 1:11 PM.
 */

class CategoryHolder(private val view: View, private val viewPool: RecyclerView.RecycledViewPool) :
    RecyclerView.ViewHolder(view) {

    fun bind(explore: ExploreItem<Any?>) {

        with(view) {
            exploreItemTitle.text = explore.title
            exploreItemRecyclerView.apply {
                layoutManager =
                    LinearLayoutManager(exploreItemRecyclerView.context, LinearLayout.HORIZONTAL, false)
//                adapter = ExploreItemAdapter(explore.items)
                setRecycledViewPool(viewPool)
            }
        }
    }
}
