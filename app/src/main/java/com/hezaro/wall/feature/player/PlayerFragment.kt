package com.hezaro.wall.feature.player

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.exoplayer2.Player
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hezaro.wall.R
import com.hezaro.wall.R.drawable
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.feature.main.MainActivity
import com.hezaro.wall.feature.main.SharedViewModel
import com.hezaro.wall.feature.search.PLAY_SINGLE_TRACK
import com.hezaro.wall.feature.search.RESUME_VIEW
import com.hezaro.wall.feature.search.SELECT_FROM_PLAYLIST
import com.hezaro.wall.feature.search.SELECT_SINGLE_TRACK
import com.hezaro.wall.feature.search.UPDATE_VIEW
import com.hezaro.wall.sdk.platform.BaseActivity
import com.hezaro.wall.sdk.platform.ext.load
import com.hezaro.wall.sdk.platform.player.MediaPlayerState
import com.hezaro.wall.sdk.platform.utils.ACTION_PLAY_PAUSE
import com.hezaro.wall.services.MediaPlayerServiceHelper
import com.hezaro.wall.utils.OnSwipeTouchListener
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

class PlayerFragment : BottomSheetDialogFragment() {
    private fun layoutId() = R.layout.fragment_player
    fun tag(): String = this::class.java.simpleName
    private var isBuffering = false
    private var isExpanded = false
    private var currentEpisode: Episode? = null
    private val vm: PlayerViewModel by inject()
    private lateinit var sharedVm: SharedViewModel
    private var behavior: BottomSheetBehavior<View>? = null
    private var showInfo = false

    companion object {
        fun getInstance() = PlayerFragment()
    }

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            sharedVm.updateSheetState(newState)
            sharedVm.playerIsOpen(behavior?.peekHeight!! > 0)

            if (isBuffering && newState == BottomSheetBehavior.STATE_DRAGGING)
                behavior?.state = BottomSheetBehavior.STATE_COLLAPSED

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
                (activity as MainActivity).openEpisodeInfo(currentEpisode!!)
                showInfo = false
            }
        }
    }

    fun setBehavior() {
        behavior = BottomSheetBehavior.from(this@PlayerFragment.view)
        behavior!!.setBottomSheetCallback(bottomSheetCallback)
        behavior!!.state = BottomSheetBehavior.STATE_HIDDEN

        miniPlayerLayout.setOnTouchListener(
            OnSwipeTouchListener(
                behavior!!
            ) {
                MediaPlayerServiceHelper.stopService(requireContext())
                closeMiniPlayer()
            }
        )
    }

    fun setPlayer(player: Player) {
        playerView.player = player
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(layoutId(), container, false)

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedVm = ViewModelProviders.of(requireActivity()).get(SharedViewModel::class.java)
        sharedVm.episode.observe(this, Observer { preparePlayer(it) })
        sharedVm.playStatus.observe(this, Observer<Int> { updatePlayingStatus(it) })
        sharedVm.collapseSheet.observe(this, Observer<Int> { behavior?.state = it })
        sharedVm.isServiceConnected.observe(this, Observer<Boolean> { if (!it) playerView.player = null })

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
            when (behavior?.state) {
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    if (!isBuffering)
                        behavior?.state = BottomSheetBehavior.STATE_EXPANDED
                }
                BottomSheetBehavior.STATE_EXPANDED -> {
                    collapse()
                }
            }
        }
        exo_rew.setOnClickListener { MediaPlayerServiceHelper.seekBackward(requireContext()) }
        exo_ffwd.setOnClickListener { MediaPlayerServiceHelper.seekForward(requireContext()) }
        playPause.setOnClickListener { if (!isBuffering) togglePause() }
        likeStatus.visibility = if (vm.userIsLogin()) View.VISIBLE else View.INVISIBLE
        bookmarkStatus.visibility = if (vm.userIsLogin()) View.VISIBLE else View.INVISIBLE
        likeStatus.setOnClickListener {

            vm.sendLikeAction(!currentEpisode!!.isLiked, currentEpisode!!.id)

            if (!currentEpisode!!.isLiked) {
                currentEpisode!!.likes++
                likeStatus.setMinAndMaxFrame(50, 100)
                likeStatus.speed = 1.0F
                likeStatus.playAnimation()
                currentEpisode!!.isLiked = true
            } else {
                currentEpisode!!.likes--
                likeStatus.setMinAndMaxFrame(50, 100)
                likeStatus.speed = -1.0F
                likeStatus.playAnimation()
                currentEpisode!!.isLiked = false
            }
        }
        bookmarkStatus.setOnClickListener {

            vm.sendBookmarkAction(!currentEpisode!!.isBookmarked, currentEpisode!!.id)

            if (!currentEpisode!!.isBookmarked) {
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
        super.onStop()
        currentEpisode?.let {
            vm.sendLastPosition(it.id, playerView.player.currentPosition)
        }
        playerView.player = null
    }

    @SuppressLint("SetTextI18n")
    fun setSpeedListener(it: Float) {
        vm.speed(it)
        speedChooser.text = "${it}x"
        MediaPlayerServiceHelper.changePlaybackSpeed(context!!, it)
    }

    private fun updatePlayingStatus(action: Int) {
        when (action) {
            MediaPlayerState.STATE_CONNECTING -> {
                isBuffering = true
                playPause.setImageResource(drawable.ic_play)
                miniPlayerProgressBar.visibility = View.VISIBLE
            }
            MediaPlayerState.STATE_PLAYING -> {
                isBuffering = false
                miniPlayerProgressBar.visibility = View.INVISIBLE
                playPause.setImageResource(drawable.ic_pause)
            }
            MediaPlayerState.STATE_ENDED -> {
                closeMiniPlayer()
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

    private fun preparePlayer(it: Pair<Int, Episode>) {
        currentEpisode = it.second

        when (it.first) {
            SELECT_SINGLE_TRACK -> {
                isBuffering = true
                MediaPlayerServiceHelper.selectEpisode(
                    requireContext(),
                    currentEpisode!!
                )
            }
            PLAY_SINGLE_TRACK -> {
                isBuffering = true
                MediaPlayerServiceHelper.playEpisode(
                    requireContext(),
                    currentEpisode!!
                )
            }
            SELECT_FROM_PLAYLIST -> {
                isBuffering = true
                MediaPlayerServiceHelper.selectEpisode(requireContext(), currentEpisode!!)
            }
            UPDATE_VIEW -> {
                isBuffering = true
                showMinimize(false)
            }
            RESUME_VIEW -> {
                // I have to do THIS : because playerView doesn't appear, At this time, I don't know why!!
                togglePause()
                togglePause()
                behavior?.peekHeight =
                    resources.getDimension(R.dimen.mini_player_height).toInt()
                (activity as BaseActivity).progressbarMargin()
                isBuffering = false
                miniPlayerProgressBar.visibility = View.INVISIBLE
                playPause.setImageResource(R.drawable.ic_pause)
            }
        }
        collapse()

        if (playerView.player != null)
            currentEpisode!!.state = playerView.player.currentPosition
        vm.saveLatestEpisode(currentEpisode!!)
        updatePlayerView()
    }

    private fun togglePause() = MediaPlayerServiceHelper.sendIntent(requireContext(), ACTION_PLAY_PAUSE)

    private fun closeMiniPlayer() {
        sharedVm.listMargin(0)
        sharedVm.progressMargin(resources.getDimension(R.dimen.progress_margin_8).toInt())
        behavior?.peekHeight = 0
    }

    private fun updatePlayerView() {
        val height = resources.getDimension(R.dimen.mini_player_height).toInt()
        sharedVm.listMargin(height)
        sharedVm.progressMargin(resources.getDimension(R.dimen.progress_margin).toInt())
        behavior?.peekHeight = height

        currentEpisode?.let {
            likeStatus.visibility = if (vm.userIsLogin()) View.VISIBLE else View.INVISIBLE
            bookmarkStatus.visibility = if (vm.userIsLogin()) View.VISIBLE else View.INVISIBLE

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
                minimize.visibility = View.INVISIBLE
                playPause.visibility = View.VISIBLE
            }
    }

    private fun collapse() {
        behavior?.state = BottomSheetBehavior.STATE_COLLAPSED
    }
}
