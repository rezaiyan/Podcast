package com.hezaro.wall.feature.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.hezaro.wall.R
import com.hezaro.wall.data.model.Podcast
import com.hezaro.wall.feature.adapter.holder.PodcastHolder

class PodcastAdapter(
    private val podcasts: ArrayList<Any> = arrayListOf(),
    private val onItemClick: (Podcast, Int) -> Unit
) : Adapter<PodcastHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PodcastHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_podcast, parent, false
        ), onItemClick
    )

    fun addPodcast(it: ArrayList<Podcast>) {
        this.podcasts.addAll(it)
        notifyItemRangeInserted(itemCount, this.podcasts.size)
    }

    override fun getItemCount() = podcasts.size

    override fun onBindViewHolder(holder: PodcastHolder, position: Int) = holder.bind(podcasts[position] as Podcast)
}