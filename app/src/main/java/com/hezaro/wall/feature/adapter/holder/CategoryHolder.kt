package com.hezaro.wall.feature.adapter.holder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.hezaro.wall.data.model.Category
import kotlinx.android.synthetic.main.item_category.view.titleCategory

/**
 * @author ali (alirezaiyann@gmail.com)
 * @since 6/25/19 1:11 PM.
 */

class CategoryHolder(private val view: View) :
    RecyclerView.ViewHolder(view) {

    fun bind(category: Category) {

        with(view) {
            titleCategory.text = category.title_fa
//            categoryCover.load(category.conver)
        }
    }
}
