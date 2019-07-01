package com.hezaro.wall.feature.adapter.holder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.hezaro.wall.data.model.CategoryItem
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.EpisodeItem
import com.hezaro.wall.data.model.Podcast
import com.hezaro.wall.data.model.PodcastItem
import com.hezaro.wall.feature.adapter.CategoryAdapter
import com.hezaro.wall.feature.adapter.EpisodeAdapter
import com.hezaro.wall.feature.adapter.PodcastAdapter
import kotlinx.android.synthetic.main.item_explore.view.exploreItemRecyclerView
import kotlinx.android.synthetic.main.item_explore.view.exploreItemTitle

/**
 * @author ali (alirezaiyann@gmail.com)
 * @since 6/25/19 1:11 PM.
 */
class ExploreHolder(
    private val view: View, viewPool: RecyclerView.RecycledViewPool,
    private val onEpisodeClick: (Episode, Int) -> Unit,
    private val onPodcastClick: (Podcast, Int) -> Unit
) :
    ViewHolder(view) {

    init {
        view.exploreItemRecyclerView.setRecycledViewPool(viewPool)
    }

    fun bindEpisode(episodeItem: EpisodeItem) {
        view.exploreItemTitle.text = episodeItem.title
        view.exploreItemRecyclerView.adapter = EpisodeAdapter(
            ArrayList(episodeItem.episodes),
            false, false,
            onEpisodeClick, onPodcastClick, 1
        )
    }

    fun bindPodcast(podcastItem: PodcastItem) {
        view.exploreItemTitle.text = podcastItem.title
        view.exploreItemRecyclerView.adapter = PodcastAdapter(
            ArrayList(podcastItem.podcasts), onPodcastClick
        )
    }

    fun bindCategory(category: CategoryItem) {
        view.exploreItemTitle.text = category.title
        view.exploreItemRecyclerView.adapter = CategoryAdapter(
            ArrayList(category.categories)
        )
    }
}
