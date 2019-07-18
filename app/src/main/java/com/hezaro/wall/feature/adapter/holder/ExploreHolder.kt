package com.hezaro.wall.feature.adapter.holder

import android.view.View
import com.hezaro.wall.data.model.CategoryItem
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.EpisodeItem
import com.hezaro.wall.data.model.Podcast
import com.hezaro.wall.data.model.PodcastItem
import com.hezaro.wall.feature.adapter.CategoryAdapter
import com.hezaro.wall.feature.adapter.EpisodeAdapter
import com.hezaro.wall.feature.adapter.PodcastAdapter
import kotlinx.android.synthetic.main.item_explore.view.exploreItemRecyclerView
import timber.log.Timber

/**
 * @author ali (alirezaiyann@gmail.com)
 * @since 6/25/19 1:11 PM.
 */
class ExploreHolder(
    private val view: View,
    private val onEpisodeClick: (Episode, Int) -> Unit,
    private val onPodcastClick: (Podcast, Int) -> Unit,
    onSowMoreClick: (String) -> Unit
) :
    BaseExploreHolder(view, onSowMoreClick) {


    fun bindEpisode(episodeItem: EpisodeItem) {
        baseBind(episodeItem)
        Timber.i("bindEpisode ${this::class.java.simpleName}")
        view.exploreItemRecyclerView.adapter = EpisodeAdapter(
            ArrayList(episodeItem.episodes),
            false, false,
            onEpisodeClick, onPodcastClick, 1
        )
    }

    fun bindPodcast(podcastItem: PodcastItem) {
        Timber.i("bindPodcast ${this::class.java.simpleName}")
        baseBind(podcastItem)
        view.exploreItemRecyclerView.adapter = PodcastAdapter(
            ArrayList(podcastItem.podcasts), onPodcastClick
        )
    }

    fun bindCategory(category: CategoryItem) {
        baseBind(category)
        Timber.i("bindCategory ${this::class.java.simpleName}")
        view.exploreItemRecyclerView.adapter = CategoryAdapter(
            ArrayList(category.categories)
        )
    }
}
