package com.hezaro.wall.feature.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hezaro.wall.R
import com.hezaro.wall.data.model.DExplore
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Podcast
import com.hezaro.wall.feature.adapter.holder.ExploreHolder

/**
 * @author ali (alirezaiyann@gmail.com)
 * @since 6/25/19 11:41 AM.
 */
class ExploreAdapter(
    private val items: DExplore,
    private val onEpisodeClick: (Episode, Int) -> Unit,
    private val onPodcastClick: (Podcast, Int) -> Unit
) :
    RecyclerView.Adapter<ExploreHolder>() {

    private val viewPool = RecyclerView.RecycledViewPool()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExploreHolder {
        return ExploreHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_explore, parent, false),
            viewPool
            , onEpisodeClick, onPodcastClick
        )
    }

    fun getItems() = items

    override fun getItemViewType(position: Int) = items.getMergedList()[position].type

    override fun onBindViewHolder(holder: ExploreHolder, position: Int) {
        Log.i("tag", "$position")
        when (getItemViewType(position)) {
            1 -> holder.bindEpisode(items.episodeItems[position])
            2 -> holder.bindPodcast(items.podcastItems[position - items.episodeItems.size])
            3 -> holder.bindCategory(
                items.categoryItems[position - (items.episodeItems.size + items.podcastItems.size)]
            )
//            3 -> (holder as ExploreHolder).bindCategory(items[position] as CategoryItem)
        }
    }

    override fun getItemCount() = items.podcastItems.size + items.episodeItems.size + items.categoryItems.size
}