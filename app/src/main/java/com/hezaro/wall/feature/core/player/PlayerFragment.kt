package com.hezaro.wall.feature.core.player

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hezaro.wall.R
import com.hezaro.wall.R.drawable
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.feature.core.main.MainActivity
import com.hezaro.wall.sdk.platform.BaseActivity
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.sdk.platform.ext.load
import com.hezaro.wall.sdk.platform.player.MediaPlayerState
import com.hezaro.wall.services.MediaPlayerServiceHelper
import com.hezaro.wall.utils.ACTION_PLAY_PAUSE
import kotlinx.android.synthetic.main.fragment_player.episodeInfo
import kotlinx.android.synthetic.main.fragment_player.logo
import kotlinx.android.synthetic.main.fragment_player.miniPlayerLayout
import kotlinx.android.synthetic.main.fragment_player.miniPlayerProgressBar
import kotlinx.android.synthetic.main.fragment_player.minimize
import kotlinx.android.synthetic.main.fragment_player.playPause
import kotlinx.android.synthetic.main.fragment_player.playerView
import kotlinx.android.synthetic.main.fragment_player.speedChooser
import kotlinx.android.synthetic.main.fragment_player.subtitle
import kotlinx.android.synthetic.main.fragment_player.title
import kotlinx.android.synthetic.main.playback_control.bookmarkStatus
import kotlinx.android.synthetic.main.playback_control.exo_ffwd
import kotlinx.android.synthetic.main.playback_control.exo_rew
import kotlinx.android.synthetic.main.playback_control.likeStatus
import org.koin.android.ext.android.inject

class PlayerFragment : BaseFragment() {
    override fun layoutId() = R.layout.fragment_player
    override fun tag(): String = this::class.java.simpleName
    private var isBuffering = false
    private var isExpanded = false
    var currentEpisode: Episode? = null
    private val vm: PlayerViewModel by inject()
    private var playerSheetBehavior: BottomSheetBehavior<View>? = null
    private val activity: MainActivity by lazy { requireActivity() as MainActivity }

    fun setBehavior(playerSheetBehavior: BottomSheetBehavior<View>) {
        this.playerSheetBehavior = playerSheetBehavior
        playerSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        playerSheetBehavior.setBottomSheetCallback(bottomSheetCallback)
    }

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (isBuffering && newState == BottomSheetBehavior.STATE_DRAGGING)
                playerSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED

            when (newState) {
                BottomSheetBehavior.STATE_EXPANDED -> {
                    isExpanded = true
                    showMinimize(true)
                }
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    isExpanded = false
                    showMinimize(false)
                }
                else -> {
                    isExpanded = false
                }
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            if (showInfo && slideOffset < 0.5F) {
                activity.openEpisodeInfo(currentEpisode!!)
                showInfo = false
            }
        }
    }
    var showInfo = false
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showMinimize(false)
        speedChooser.text = "${vm.defaultSpeed()}x"
        val speedPicker = SpeedPicker(context!!, ::setSpeedListener)
        speedChooser.setOnClickListener {
            speedPicker.show(vm.defaultSpeed())
        }
        episodeInfo.setOnClickListener {
            collapse()
            showInfo = true
        }
        miniPlayerLayout.setOnClickListener {
            when (playerSheetBehavior?.state) {
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    if (!isBuffering)
                        playerSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                }
                BottomSheetBehavior.STATE_EXPANDED -> {
                    collapse()
                }
            }
        }
        minimize.setOnClickListener { collapse() }
        exo_rew.setOnClickListener { MediaPlayerServiceHelper.seekBackward(requireContext()) }
        exo_ffwd.setOnClickListener { MediaPlayerServiceHelper.seekForward(requireContext()) }
        playPause.setOnClickListener { if (!isBuffering) doOnPlayer(ACTION_PLAY_PAUSE) }
        likeStatus.setOnClickListener {

            vm.sendLikeAction(!currentEpisode!!.isLiked, currentEpisode!!.id)

            if (!currentEpisode!!.isLiked) {
                currentEpisode!!.votes++
                likeStatus.setMinAndMaxFrame(50, 100)
                likeStatus.speed = 1.0F
                likeStatus.playAnimation()
                currentEpisode!!.isLiked = true
            } else {
                currentEpisode!!.votes--
                likeStatus.setMinAndMaxFrame(50, 100)
                likeStatus.speed = -1.0F
                likeStatus.playAnimation()
                currentEpisode!!.isLiked = false
            }
        }
        bookmarkStatus.setOnClickListener {

            if (!currentEpisode!!.isLiked) {
                bookmarkStatus.setMinAndMaxFrame(0, 50)
                bookmarkStatus.speed = 1.0F
                bookmarkStatus.playAnimation()
            } else {
                bookmarkStatus.setMinAndMaxFrame(0, 50)
                bookmarkStatus.speed = -1.0F
                bookmarkStatus.playAnimation()
            }
        }

    }

    override fun onStop() {
        currentEpisode?.let {
            vm.sendLastPosition(it.id, playerView.player.currentPosition)
        }
        super.onStop()
    }
    @SuppressLint("SetTextI18n")
    fun setSpeedListener(it: Float) {
        vm.speed(it)
        speedChooser.text = "${it}x"
        MediaPlayerServiceHelper.changePlaybackSpeed(context!!, it)
    }

    private fun doOnPlayer(action: String) {
        MediaPlayerServiceHelper.sendIntent(requireContext(), action)
    }

    fun updateEpisodeView(episode: Episode) {
        this.currentEpisode = episode
        updatePlayerView()
        currentEpisode!!.state = playerView.player.currentPosition
        vm.saveLatestEpisode(currentEpisode!!)
    }

    fun updatePlayingStatus(action: Int) {
        when (action) {
            MediaPlayerState.STATE_CONNECTING or MediaPlayerState.STATE_CONNECTING -> {
                isBuffering = true
                playPause.setImageResource(drawable.ic_play)
                miniPlayerProgressBar.visibility = View.VISIBLE
            }
            MediaPlayerState.STATE_PLAYING -> {
                isBuffering = false
                miniPlayerProgressBar.visibility = View.INVISIBLE
                playPause.setImageResource(drawable.ic_pause)
            }
            MediaPlayerState.STATE_PAUSED -> {
                isBuffering = false
                miniPlayerProgressBar.visibility = View.INVISIBLE
                playPause.setImageResource(drawable.ic_play)
                currentEpisode?.let {
                    it.state = playerView.player.currentPosition
                    vm.saveLatestEpisode(it)
                }
            }
        }
    }

    fun isOpen() = currentEpisode != null

    fun isExpand() = playerSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED
    fun isCollapsed() = playerSheetBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED

    fun collapse() {
        playerSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun openMiniPlayer(episode: Episode) {
        this.currentEpisode = episode
        (activity as BaseActivity).progressbarMargin()
        playerSheetBehavior?.peekHeight =
            resources.getDimension(R.dimen.mini_player_height).toInt()
        MediaPlayerServiceHelper.playEpisode(requireContext(), episode)
        updatePlayerView()
        collapse()
    }

    fun updateMiniPlayer(episode: Episode) {
        this.currentEpisode = episode
        doOnPlayer(ACTION_PLAY_PAUSE)
        doOnPlayer(ACTION_PLAY_PAUSE)
        playerSheetBehavior?.peekHeight =
            resources.getDimension(R.dimen.mini_player_height).toInt()
        updatePlayerView()
        collapse()
        (activity as BaseActivity).progressbarMargin()
        isBuffering = false
        miniPlayerProgressBar.visibility = View.INVISIBLE
        playPause.setImageResource(R.drawable.ic_pause)
    }

    fun onLoadLastPlayedEpisode(episode: Episode) {
        if (!playerView.player.playWhenReady) {
            currentEpisode = episode
            openMiniPlayer(episode)
        }
    }

    private fun updatePlayerView() {
        currentEpisode!!.let {
            title.text = it.title
            subtitle.text = it.podcast.creator
            logo.load(it.cover)
            if (it.isLiked)
                likeStatus.frame = 100
            else likeStatus.frame = 50
            if (it.isLiked)
                bookmarkStatus.frame = 5
            else bookmarkStatus.frame = 0
        }
    }

    private fun showMinimize(beMinimize: Boolean) {

        if (minimize != null && playPause != null)
            if (beMinimize) {
                minimize.visibility = View.VISIBLE
                playPause.visibility = View.INVISIBLE
            } else {
                playPause.visibility = View.VISIBLE
                minimize.visibility = View.INVISIBLE
            }
    }

    override fun onBackPressed() {
        collapse()
    }

    fun ishidden() = playerSheetBehavior?.state == BottomSheetBehavior.STATE_HIDDEN
}
