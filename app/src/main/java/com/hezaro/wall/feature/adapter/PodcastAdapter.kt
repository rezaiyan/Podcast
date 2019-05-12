package com.hezaro.wall.feature.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.hezaro.wall.R
import com.hezaro.wall.data.model.Podcast
import com.hezaro.wall.feature.adapter.PodcastAdapter.ItemHolder
import com.hezaro.wall.sdk.platform.ext.load
import com.hezaro.wall.utils.RoundRectTransform
import kotlinx.android.synthetic.main.item_podcast.view.podcastCover
import kotlinx.android.synthetic.main.item_podcast.view.podcastName

class PodcastAdapter(
    val podcasts: ArrayList<Podcast> = arrayListOf(),
    private val onItemClick: (Podcast, Int) -> Unit
) : Adapter<ItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ItemHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_podcast, parent, false
        )
    )

    fun addPodcast(it: ArrayList<Podcast>) {
        this.podcasts.addAll(it)
        notifyItemRangeInserted(itemCount, this.podcasts.size)
    }

    override fun getItemCount() = podcasts.size

    override fun onBindViewHolder(holder: ItemHolder, position: Int) = holder.bind(podcasts[position])

    inner class ItemHolder(view: View) : ViewHolder(view) {

        private val transform = RoundRectTransform()
        fun bind(podcast: Podcast) {

            itemView.let {
                it.podcastCover.load(podcast.cover, transform)
                it.podcastName.text = podcast.title
                it.setOnClickListener { onItemClick(podcast, adapterPosition) }
            }
        }
    }
}