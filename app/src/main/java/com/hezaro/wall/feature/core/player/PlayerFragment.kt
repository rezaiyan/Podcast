package com.hezaro.wall.feature.core.player

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hezaro.wall.R
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.feature.core.main.MainActivity
import com.hezaro.wall.sdk.platform.BaseActivity
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.sdk.platform.player.MediaPlayerState
import com.hezaro.wall.services.MediaPlayerServiceHelper
import com.hezaro.wall.utils.ACTION_EPISODE
import com.hezaro.wall.utils.ACTION_EPISODE_GET
import com.hezaro.wall.utils.ACTION_PLAYER
import com.hezaro.wall.utils.ACTION_PLAYER_STATUS
import com.hezaro.wall.utils.ACTION_PLAY_PAUSE
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_player.episodeInfo
import kotlinx.android.synthetic.main.fragment_player.logo
import kotlinx.android.synthetic.main.fragment_player.miniPlayerLayout
import kotlinx.android.synthetic.main.fragment_player.miniPlayerProgressBar
import kotlinx.android.synthetic.main.fragment_player.minimize
import kotlinx.android.synthetic.main.fragment_player.playPause
import kotlinx.android.synthetic.main.fragment_player.speedChooser
import kotlinx.android.synthetic.main.fragment_player.subtitle
import kotlinx.android.synthetic.main.fragment_player.title
import kotlinx.android.synthetic.main.playback_control.exo_ffwd
import kotlinx.android.synthetic.main.playback_control.exo_rew
import org.koin.android.ext.android.inject

class PlayerFragment : BaseFragment() {
    override fun layoutId() = R.layout.fragment_player
    override fun tag(): String = this::class.java.simpleName
    private var isBuffering = false
    private var isExpanded = false
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

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showMinimize(false)
        speedChooser.text = "${vm.defaultSpeed()}x"
        val speedPicker = SpeedPicker(context!!, ::setSpeedListener)
        speedChooser.setOnClickListener {
            speedPicker.show(vm.defaultSpeed())
        }
        episodeInfo.setOnClickListener { activity.episode() }
        miniPlayerLayout.setOnClickListener {
            when (playerSheetBehavior?.state) {
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    if (!isBuffering)
                        playerSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                }
                BottomSheetBehavior.STATE_EXPANDED -> {
                    playerSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }
        }
        minimize.setOnClickListener { playerSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED }
        exo_rew.setOnClickListener { MediaPlayerServiceHelper.seekBackward(requireContext()) }
        exo_ffwd.setOnClickListener { MediaPlayerServiceHelper.seekForward(requireContext()) }
        playPause.setOnClickListener { if (!isBuffering) doOnPlayer(ACTION_PLAY_PAUSE) }
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

    private var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent != null) {

                when (intent.action) {
                    ACTION_EPISODE -> {
                        val episode = intent.getParcelableExtra<Episode>(ACTION_EPISODE_GET)
                        updatePlayerView(episode)
                    }
                    ACTION_PLAYER -> {
                        val action = intent.getIntExtra(ACTION_PLAYER_STATUS, MediaPlayerState.STATE_IDLE)
                        when (action) {
                            MediaPlayerState.STATE_CONNECTING or MediaPlayerState.STATE_CONNECTING -> {
                                isBuffering = true
                                playPause.setImageResource(R.drawable.ic_play)
                                miniPlayerProgressBar.visibility = View.VISIBLE
                            }
                            MediaPlayerState.STATE_PLAYING -> {
                                isBuffering = false
                                miniPlayerProgressBar.visibility = View.INVISIBLE
                                playPause.setImageResource(R.drawable.ic_pause)
                            }
                            MediaPlayerState.STATE_PAUSED -> {
                                isBuffering = false
                                miniPlayerProgressBar.visibility = View.INVISIBLE
                                playPause.setImageResource(R.drawable.ic_play)
                            }
                        }
                    }
                }
            }
        }
    }

    fun expand() {
        playerSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun isExpand() = playerSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED

    fun collapse() {
        playerSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun openMiniPlayer(episode: Episode) {
        (activity as BaseActivity).progressbarMargin()
        playerSheetBehavior?.peekHeight =
            resources.getDimension(R.dimen.mini_player_height).toInt()
        MediaPlayerServiceHelper.playEpisode(requireContext(), episode)
        updatePlayerView(episode)
        collapse()
    }

    fun updateMiniPlayer(episode: Episode) {
        (activity as BaseActivity).progressbarMargin()
        doOnPlayer(ACTION_PLAY_PAUSE)
        doOnPlayer(ACTION_PLAY_PAUSE)
        playerSheetBehavior?.peekHeight =
            resources.getDimension(R.dimen.mini_player_height).toInt()
        updatePlayerView(episode)
        collapse()
        isBuffering = false
        miniPlayerProgressBar.visibility = View.INVISIBLE
        playPause.setImageResource(R.drawable.ic_pause)
    }

    private fun updatePlayerView(episode: Episode) {
        title.text = episode.title
        subtitle.text = episode.podcast?.creator
        Picasso.get().load(episode.cover).into(logo)
    }

    private fun showMinimize(beMinimize: Boolean) {

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

    override fun onStart() {
        super.onStart()
        val iff = IntentFilter(ACTION_EPISODE)
        iff.addAction(ACTION_PLAYER)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(receiver, iff)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver)
    }

    fun ishidden() = playerSheetBehavior?.state == BottomSheetBehavior.STATE_HIDDEN
}
