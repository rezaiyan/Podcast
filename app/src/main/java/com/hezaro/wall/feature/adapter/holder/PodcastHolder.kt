package com.hezaro.wall.feature.adapter.holder

import android.view.View
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.hezaro.wall.data.model.Podcast
import com.hezaro.wall.sdk.platform.ext.load
import com.hezaro.wall.utils.RoundRectTransform
import kotlinx.android.synthetic.main.item_podcast.view.podcastCover
import kotlinx.android.synthetic.main.item_podcast.view.podcastName

/**
 * @author ali (alirezaiyann@gmail.com)
 * @since 6/25/19 1:11 PM.
 */
class PodcastHolder(
    view: View,
    private val onItemClick: (Podcast, Int) -> Unit
) : ViewHolder(view) {

    private val transform = RoundRectTransform()
    fun bind(podcast: Podcast) {

        itemView.let {
            it.podcastCover.load(podcast.cover, transformation = transform)
            it.podcastName.text = podcast.title
            it.setOnClickListener { onItemClick(podcast, adapterPosition) }
        }
    }
}