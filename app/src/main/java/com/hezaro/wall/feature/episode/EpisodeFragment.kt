package com.hezaro.wall.feature.episode

import android.app.AlertDialog
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hezaro.wall.R
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.feature.main.MainActivity
import com.hezaro.wall.feature.main.SharedViewModel
import com.hezaro.wall.feature.search.UPDATE_VIEW
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.sdk.platform.ext.load
import com.hezaro.wall.sdk.platform.player.download.DownloadTracker
import com.hezaro.wall.sdk.platform.player.download.PlayerDownloadHelper
import com.hezaro.wall.sdk.platform.utils.PARAM_EPISODE
import com.hezaro.wall.sdk.platform.utils.PullDismissLayout
import kotlinx.android.synthetic.main.fragment_episode.description
import kotlinx.android.synthetic.main.fragment_episode.downloadStatus
import kotlinx.android.synthetic.main.fragment_episode.episodeCover
import kotlinx.android.synthetic.main.fragment_episode.episodeTitle
import kotlinx.android.synthetic.main.fragment_episode.likeCount
import kotlinx.android.synthetic.main.fragment_episode.playedCount
import kotlinx.android.synthetic.main.fragment_episode.podcastCover
import kotlinx.android.synthetic.main.fragment_episode.podcastTitle
import kotlinx.android.synthetic.main.fragment_episode.pullLayout
import org.koin.android.ext.android.inject

class EpisodeFragment : BaseFragment(), PullDismissLayout.Listener, DownloadTracker.Listener {

    private val downloadHelper: PlayerDownloadHelper by inject()
    private val downloader by lazy { downloadHelper.getDownloadTracker()!! }

    override fun onDismissed() = activity.onBackPressed()
    override fun onShouldInterceptTouchEvent() = sharedVm.sheetState.value == BottomSheetBehavior.STATE_EXPANDED

    override fun layoutId() = R.layout.fragment_episode
    override fun tag(): String = this::class.java.simpleName

    private val activity: MainActivity by lazy { requireActivity() as MainActivity }

    private val vm: EpisodeViewModel by inject()
    private lateinit var sharedVm: SharedViewModel
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
        sharedVm = ViewModelProviders.of(requireActivity()).get(SharedViewModel::class.java)
        currentEpisode = arguments?.getParcelable(PARAM_EPISODE)
        sharedVm.episode.observe(this, Observer {
            currentEpisode = it.second
            updateView()
        })

        updateView()
        podcastTitle.setOnClickListener { activity.openPodcastInfo(currentEpisode!!.podcast) }
        downloadStatus.setOnClickListener {
            val uri = Uri.parse(currentEpisode!!.source)
            val title = currentEpisode!!.title
            if (downloader.isDownloaded(uri))
                removeDownloadDialog(title, uri)
            else
                downloader.startDownload(activity, title, uri)
        }
    }

    private fun removeDownloadDialog(title: String, uri: Uri) {
        val alertDialog = AlertDialog.Builder(context)
        alertDialog.setMessage("$title جذف شود؟ ")
        alertDialog.setNegativeButton("خیر") { _a, _ -> _a.dismiss() }
        alertDialog.setPositiveButton("بله") { _, _ ->
            vm.delete(currentEpisode!!)
            downloader.removeDownload(uri, title)
        }
        alertDialog.create().show()
    }

    override fun onDownloadsChanged(isDownload: Boolean) {
        val downloaded = downloader.isDownloaded(Uri.parse(currentEpisode!!.source))
        if (downloaded) {
            vm.save(currentEpisode!!)
            currentEpisode!!.isDownloaded = 1
            downloadStatus.setMinAndMaxProgress(0.12f, 0.74f)
            downloadStatus.speed = 1.0F
            downloadStatus.playAnimation()
        } else {
            vm.delete(currentEpisode!!)
            currentEpisode!!.isDownloaded = 0
            downloadStatus.setMinAndMaxProgress(0.12f, 0.74f)
            downloadStatus.speed = -1.0F
            downloadStatus.playAnimation()
        }

        sharedVm.notifyEpisode(Pair(UPDATE_VIEW, currentEpisode!!))
    }

    override fun onStart() {
        pullLayout.setListener(this)
        downloader.addListener(this)
        super.onStart()
    }

    override fun onStop() {
        pullLayout.removeListener()
        downloader.removeListener(this)
        super.onStop()
    }

    private fun updateView() {
        val downloaded = downloader.isDownloaded(Uri.parse(currentEpisode!!.source))
        if (downloaded)
            downloadStatus.progress = 0.74f
        else downloadStatus.progress = 0.12f

        currentEpisode?.let {

            podcastCover.load(it.podcast.cover)
            episodeCover.load(it.cover)
            episodeTitle.text = it.title
            podcastTitle.text = it.podcast.title
            playedCount.text = it.views.toString()
            likeCount.text = it.likes.toString()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                description.text = Html.fromHtml(it.description, Html.FROM_HTML_MODE_LEGACY)
            } else {
                description.text = Html.fromHtml(it.description)
            }
            description.movementMethod = LinkMovementMethod.getInstance()

        }
    }
}