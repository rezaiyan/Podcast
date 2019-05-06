package com.hezaro.wall.feature.episode

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.hezaro.wall.R
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.feature.core.main.MainActivity
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.sdk.platform.ext.load
import com.hezaro.wall.sdk.platform.ext.loadBlur
import com.hezaro.wall.sdk.platform.player.MediaPlayerState
import com.hezaro.wall.sdk.platform.player.download.DownloadTracker
import com.hezaro.wall.sdk.platform.player.download.PlayerDownloadHelper
import com.hezaro.wall.sdk.platform.utils.PullDismissLayout
import com.hezaro.wall.utils.ACTION_EPISODE
import com.hezaro.wall.utils.ACTION_EPISODE_GET
import com.hezaro.wall.utils.ACTION_PLAYER
import com.hezaro.wall.utils.ACTION_PLAYER_STATUS
import com.hezaro.wall.utils.PARAM_EPISODE
import kotlinx.android.synthetic.main.fragment_episode.description
import kotlinx.android.synthetic.main.fragment_episode.downloadStatus
import kotlinx.android.synthetic.main.fragment_episode.episodeCover
import kotlinx.android.synthetic.main.fragment_episode.episodeTitle
import kotlinx.android.synthetic.main.fragment_episode.playedCount
import kotlinx.android.synthetic.main.fragment_episode.podcastCover
import kotlinx.android.synthetic.main.fragment_episode.podcastTitle
import kotlinx.android.synthetic.main.fragment_episode.podcasterName
import kotlinx.android.synthetic.main.fragment_episode.pullLayout
import kotlinx.android.synthetic.main.fragment_episode.voteCount
import org.koin.android.ext.android.inject

class EpisodeFragment : BaseFragment(), PullDismissLayout.Listener, DownloadTracker.Listener {

    override fun onDismissed() = activity.onBackPressed()
    override fun onShouldInterceptTouchEvent() = activity.isPlayerExpand()

    override fun layoutId() = R.layout.fragment_episode
    override fun tag(): String = this::class.java.simpleName

    private val activity: MainActivity by lazy { requireActivity() as MainActivity }

    private val downloadHelper: PlayerDownloadHelper by inject()
    private val vm: EpisodeViewModel by inject()

    private var downloader: DownloadTracker? = null
    private var currentEpisode: Episode? = null

    companion object {
        fun newInstance(episode: Episode) = EpisodeFragment().also {
            it.arguments = Bundle().apply {
                putParcelable(PARAM_EPISODE, episode)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        downloader = downloadHelper.getDownloadTracker()!!
        downloader!!.addListener(this)
        currentEpisode = arguments?.getParcelable(PARAM_EPISODE)
        pullLayout.setListener(this)

        updateView()
        episodeCover.setOnClickListener {
            val uri = Uri.parse(currentEpisode!!.source)
            val title = currentEpisode!!.title
            if (downloader!!.isDownloaded(uri))
                removeDownloadDialog(title, uri)
            else
                downloader!!.startDownload(activity, title, uri)
        }
    }

    private fun removeDownloadDialog(title: String, uri: Uri) {
        val alertDialog = AlertDialog.Builder(context, android.R.style.Widget_Material_Light_ButtonBar_AlertDialog)
        alertDialog.setMessage("حذف از دانلودها")
        alertDialog.setNegativeButton("خیر") { _a, _ -> _a.dismiss() }
        alertDialog.setNegativeButton("بله") { _, _ -> downloader!!.removeDownload(uri, title) }
        alertDialog.create().show()
    }

    override fun onDownloadsChanged(isDownload: Boolean) {
        if (downloader!!.isDownloaded(Uri.parse(currentEpisode!!.source)))
            vm.save(currentEpisode!!)
        else vm.delete(currentEpisode!!)
        updateView()
    }

    override fun onStart() {
        LocalBroadcastManager.getInstance(context!!)
            .registerReceiver(receiver, IntentFilter(ACTION_EPISODE).also { it.addAction(ACTION_PLAYER) })
        super.onStart()
    }

    override fun onStop() {
        downloader!!.removeListener(this)
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(receiver)
        super.onStop()
    }

    private var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent != null) {

                when (intent.action) {
                    ACTION_EPISODE -> {
                        currentEpisode = intent.getParcelableExtra(ACTION_EPISODE_GET)
                        updateView()
                    }
                    ACTION_PLAYER -> {
                        val action = intent.getIntExtra(ACTION_PLAYER_STATUS, MediaPlayerState.STATE_IDLE)
                        when (action) {
                            MediaPlayerState.STATE_CONNECTING or MediaPlayerState.STATE_CONNECTING -> {
                            }
                            MediaPlayerState.STATE_PLAYING -> {
                            }
                            MediaPlayerState.STATE_PAUSED -> {
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateView() {
        val downloaded = downloader!!.isDownloaded(Uri.parse(currentEpisode!!.source))
        if (downloaded)
            downloadStatus.text = "دانلود شده"
        else downloadStatus.text = "دانلود نشده"

        currentEpisode?.let {

            podcastCover.loadBlur(it.podcast.cover)
            episodeCover.load(it.cover)
            episodeTitle.text = it.title
            podcastTitle.text = it.podcast.title
            podcasterName.text = it.creator
            playedCount.text = it.views.toString()
            voteCount.text = it.votes.toString()
            description.loadDataWithBaseURL(null, it.description, "text/html", "UTF-8", null)

        }
    }
}