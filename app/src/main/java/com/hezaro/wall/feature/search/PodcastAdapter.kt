package com.hezaro.wall.feature.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.hezaro.wall.R
import com.hezaro.wall.data.model.Podcast
import com.hezaro.wall.sdk.platform.ext.load
import kotlinx.android.synthetic.main.item_podcast.view.podcastCover
import kotlinx.android.synthetic.main.item_podcast.view.podcastName

class PodcastAdapter(
    val podcasts: MutableList<Podcast> = mutableListOf(),
    private val onItemClick: (Podcast, Int) -> Unit
) : Adapter<PodcastAdapter.ItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ItemHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_podcast, parent, false
        )
    )

    fun addPodcast(it: MutableList<Podcast>) {
        this.podcasts.addAll(it)
        notifyItemRangeInserted(itemCount, this.podcasts.size)
    }

    override fun getItemCount() = podcasts.size

    override fun onBindViewHolder(holder: PodcastAdapter.ItemHolder, position: Int) = holder.bind(podcasts[position])

    inner class ItemHolder(view: View) : ViewHolder(view) {

        fun bind(podcast: Podcast) {

            itemView.let {
                it.podcastCover.load(podcast.cover)
                it.podcastName.text = podcast.title
                it.setOnClickListener { onItemClick(podcast, adapterPosition) }
            }
        }
    }
}