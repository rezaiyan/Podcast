package com.hezaro.wall.feature.adapter.holder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.hezaro.wall.data.model.ExploreItem
import com.hezaro.wall.sdk.platform.ext.hide
import com.hezaro.wall.sdk.platform.ext.show
import kotlinx.android.synthetic.main.item_explore.view.exploreItemTitle
import kotlinx.android.synthetic.main.item_explore.view.exploreShowMoreTitle

/**
 * @author ali (alirezaiyann@gmail.com)
 * @since 7/5/19 9:17 PM.
 */
abstract class BaseExploreHolder(
    private val view: View, private val viewPool: RecyclerView.RecycledViewPool
) :
    ViewHolder(view) {


    fun baseBind(exploreItem: ExploreItem) {
        with(view) {
            //            exploreItemRecyclerView.setRecycledViewPool(viewPool)
            if (exploreItem.show_more)
                exploreShowMoreTitle.show()
            else exploreShowMoreTitle.hide()

            exploreItemTitle.text = exploreItem.title
        }
    }
}