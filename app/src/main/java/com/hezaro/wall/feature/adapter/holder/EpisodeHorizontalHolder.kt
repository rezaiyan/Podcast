package com.hezaro.wall.feature.adapter.holder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Podcast
import com.hezaro.wall.sdk.platform.ext.load
import com.hezaro.wall.utils.RoundRectTransform
import kotlinx.android.synthetic.main.item_episode_h.view.episodeSubtitle
import kotlinx.android.synthetic.main.item_episode_h.view.episodeTitle
import kotlinx.android.synthetic.main.item_episode_h.view.logo

/**
 * @author ali (alirezaiyann@gmail.com)
 * @since 6/25/19 1:10 PM.
 */

class EpisodeHorizontalHolder(
    view: View,
    private val onItemClick: (Episode, Int) -> Unit,
    private val longClickListener: (Podcast, Int) -> Unit
) : RecyclerView.ViewHolder(view) {


    fun bind(episode: Episode) {

        itemView.let {
            episode.run {
                it.logo.load(cover, transformation = RoundRectTransform())
                it.episodeTitle.text = title
                it.episodeSubtitle.text = podcast.title
                it.setOnClickListener { onItemClick(this, adapterPosition) }
                it.setOnLongClickListener { longClickListener(podcast, podcast.id.toInt());true }

            }

        }
    }
}
