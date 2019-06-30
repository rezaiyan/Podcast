package com.hezaro.wall.feature.adapter.holder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.ExploreItem
import com.hezaro.wall.data.model.Podcast
import com.hezaro.wall.feature.adapter.EpisodeAdapter
import kotlinx.android.synthetic.main.item_explore.view.exploreItemRecyclerView

/**
 * @author ali (alirezaiyann@gmail.com)
 * @since 6/25/19 1:11 PM.
 */
class ExploreHolder(
    private val view: View, private val viewPool: RecyclerView.RecycledViewPool,
    private val onEpisodeClick: (Episode, Int) -> Unit,
    private val onPodcastClick: (Podcast, Int) -> Unit
) :
    BaseNestedHolder(view, viewPool) {

    fun bindBest(best: ExploreItem.Best) {
        view.exploreItemRecyclerView.adapter = EpisodeAdapter(
            ArrayList(best.items),
            false, false,
            onEpisodeClick, onPodcastClick, 1
        )
    }

    fun bindLast(last: ExploreItem.Last) {
        view.exploreItemRecyclerView.adapter = EpisodeAdapter(
            ArrayList(last.items),
            false, false,
            onEpisodeClick, onPodcastClick, 1
        )
    }

    fun bindBanner(recommended: ExploreItem.Recommended) {
    }

    fun bindCategory(category: ExploreItem.Category) {
    }
}
