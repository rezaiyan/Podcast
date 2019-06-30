package com.hezaro.wall.feature.adapter.holder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.hezaro.wall.data.model.DOWNLOADED
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Podcast
import com.hezaro.wall.sdk.platform.ext.hide
import com.hezaro.wall.sdk.platform.ext.load
import com.hezaro.wall.sdk.platform.ext.show
import com.hezaro.wall.utils.RoundRectTransform
import ir.smartlab.persindatepicker.util.PersianCalendar
import kotlinx.android.synthetic.main.item_episode.view.bookmarkStatus
import kotlinx.android.synthetic.main.item_episode.view.date
import kotlinx.android.synthetic.main.item_episode.view.downloadStatus
import kotlinx.android.synthetic.main.item_episode.view.logo
import kotlinx.android.synthetic.main.item_episode.view.podcaster
import kotlinx.android.synthetic.main.item_episode.view.title

/**
 * @author ali (alirezaiyann@gmail.com)
 * @since 6/25/19 1:10 PM.
 */

class EpisodeHolder(
    view: View,
    private val isDownloadList: Boolean,
    private val isBookmarkList: Boolean,
    private val onItemClick: (Episode, Int) -> Unit,
    private val longClickListener: (Podcast, Int) -> Unit
) : RecyclerView.ViewHolder(view) {


    private val calendar = PersianCalendar()

    fun bind(episode: Episode) {

        itemView.let {
            episode.run {
                it.logo.load(cover, transformation = RoundRectTransform())
                if (downloadStatus == DOWNLOADED && isDownloadList.not()) {
                    it.downloadStatus.show()
                    it.downloadStatus.progress = 0.74f
                } else it.downloadStatus.hide()
                if (isBookmarked && isBookmarkList.not()) {
                    it.bookmarkStatus.show()
                    it.bookmarkStatus.progress = 1.0f
                } else it.bookmarkStatus.hide()
                it.title.text = title
                calendar.timeInMillis = getPublishTime()
                it.date.text = calendar.persianLongDate
                it.podcaster.text = podcast.title
                it.setOnClickListener { onItemClick(this, adapterPosition) }
                it.setOnLongClickListener { longClickListener(podcast, podcast.id.toInt());true }

            }

        }
    }
}
