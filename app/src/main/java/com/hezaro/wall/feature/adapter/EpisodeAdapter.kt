package com.hezaro.wall.feature.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hezaro.wall.R
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.feature.adapter.EpisodeAdapter.ItemHolder
import com.hezaro.wall.sdk.platform.ext.load
import com.hezaro.wall.utils.RoundRectTransform
import ir.smartlab.persindatepicker.util.PersianCalendar
import kotlinx.android.synthetic.main.item_episode.view.bookmarkStatus
import kotlinx.android.synthetic.main.item_episode.view.date
import kotlinx.android.synthetic.main.item_episode.view.downloadStatus
import kotlinx.android.synthetic.main.item_episode.view.logo
import kotlinx.android.synthetic.main.item_episode.view.podcaster
import kotlinx.android.synthetic.main.item_episode.view.title

class EpisodeAdapter(
    val episodes: ArrayList<Episode> = arrayListOf(),
    private val isDownloadList: Boolean = false,
    private val onItemClick: (Episode, Int) -> Unit
) :
    RecyclerView.Adapter<ItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ItemHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_episode, parent, false)
    )

    override fun getItemCount() = episodes.size

    override fun onBindViewHolder(holder: ItemHolder, position: Int) = holder.bind(episodes[position])

    fun updateList(episodes: ArrayList<Episode>) {
        this.episodes.addAll(episodes)
        notifyItemRangeInserted(itemCount, this.episodes.size)
    }

    fun clearAndAddEpisode(episodes: ArrayList<Episode>) {
        this.episodes.clear()
        this.episodes.addAll(episodes)
        notifyItemRangeInserted(0, this.episodes.size)
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

    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val calendar = PersianCalendar()

        fun bind(episode: Episode) {
            itemView.let {

                episode.run {
                    it.logo.load(cover, transformation = RoundRectTransform())
                    if (isDownloaded == 1 && isDownloadList.not()) {
                        it.downloadStatus.visibility = View.VISIBLE
                        it.downloadStatus.progress = 0.74f
                    } else it.downloadStatus.visibility = View.INVISIBLE
                    if (isBookmarked) {
                        it.bookmarkStatus.visibility = View.VISIBLE
                        it.bookmarkStatus.progress = 1.0f
                    } else it.bookmarkStatus.visibility = View.INVISIBLE
                    it.title.text = title
                    calendar.timeInMillis = publishedTime
                    it.date.text = calendar.persianLongDate
                    it.podcaster.text = podcast.title
                    it.setOnClickListener { onItemClick(this, adapterPosition) }

                }

            }
        }
    }
}