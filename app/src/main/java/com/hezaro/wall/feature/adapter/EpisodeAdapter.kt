package com.hezaro.wall.feature.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.hezaro.wall.R
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Podcast
import com.hezaro.wall.feature.adapter.holder.EpisodeHolder
import com.hezaro.wall.feature.adapter.holder.EpisodeHorizontalHolder

class EpisodeAdapter(
    val episodes: ArrayList<Episode> = arrayListOf(),
    private val isDownloadList: Boolean = false,
    private val isBookmarkList: Boolean = false,
    private val onItemClick: (Episode, Int) -> Unit,
    private val longClickListener: (Podcast, Int) -> Unit
    , private val viewType: Int = 0
) :
    RecyclerView.Adapter<ViewHolder>() {

    fun getEpisodeList(): ArrayList<Episode> {
        val list = arrayListOf<Episode>()
        episodes.forEach {
            list.add(it)
        }
        return list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        when (this@EpisodeAdapter.viewType) {
            0 -> EpisodeHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_episode, parent, false),
                isDownloadList,
                isBookmarkList,
                onItemClick,
                longClickListener
            )
            else -> EpisodeHorizontalHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_episode_h, parent, false),
                onItemClick,
                longClickListener
            )
        }

    override fun getItemCount() = episodes.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        when {
            (holder is EpisodeHolder) -> holder.bind(episodes[position])
            (holder is EpisodeHorizontalHolder) -> holder.bind(episodes[position])
        }
    }

    fun updateList(episodes: ArrayList<Episode>) {
        this.episodes.addAll(episodes)
        notifyItemRangeInserted(itemCount, this.episodes.size)
    }

    fun clearAndAddEpisode(episodes: ArrayList<Episode>) {
        this.episodes.clear()
        this.episodes.addAll(episodes)
        notifyDataSetChanged()
    }

    fun clearAll() {
        episodes.clear()
        notifyDataSetChanged()
    }

    fun updateRow(e: Episode) {
        if (episodes.contains(e)) {
            val index = episodes.indexOf(e)
            episodes[index] = e
            notifyItemChanged(index)
        }
    }
}