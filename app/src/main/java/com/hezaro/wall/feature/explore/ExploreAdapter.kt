package com.hezaro.wall.feature.explore

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hezaro.wall.R
import com.hezaro.wall.data.model.Episode
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_explore.view.logo
import kotlinx.android.synthetic.main.item_explore.view.podcaster
import kotlinx.android.synthetic.main.item_explore.view.title

class ExploreAdapter(
    private val episodes: MutableList<Episode> = mutableListOf(),
    private val onItemClick: (Episode) -> Unit
) :
    RecyclerView.Adapter<ExploreAdapter.ItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ItemHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_explore, parent, false)
    )

    override fun getItemCount() = episodes.size

    override fun onBindViewHolder(holder: ItemHolder, position: Int) = holder.bind(episodes[position])

    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(episode: Episode) {
            Picasso.get().load(episode.poster).into(itemView.logo)
            itemView.title.text = episode.title
            itemView.podcaster.text = episode.title
            itemView.setOnClickListener { onItemClick(episode) }
        }
    }
}