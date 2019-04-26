package com.hezaro.wall.feature.core.player

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
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.sdk.platform.player.MediaPlayerState
import com.hezaro.wall.services.ACTION_PLAY_PAUSE
import com.hezaro.wall.services.MediaPlayerServiceHelper
import com.hezaro.wall.utils.ACTION_EPISODE
import com.hezaro.wall.utils.ACTION_EPISODE_GET
import com.hezaro.wall.utils.ACTION_PLAYER
import com.hezaro.wall.utils.ACTION_PLAYER_STATUS
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_player.logo
import kotlinx.android.synthetic.main.fragment_player.miniPlayerLayout
import kotlinx.android.synthetic.main.fragment_player.miniPlayerProgressBar
import kotlinx.android.synthetic.main.fragment_player.minimize
import kotlinx.android.synthetic.main.fragment_player.playPause
import kotlinx.android.synthetic.main.fragment_player.subtitle
import kotlinx.android.synthetic.main.fragment_player.title
import kotlinx.android.synthetic.main.playback_control.exo_ffwd
import kotlinx.android.synthetic.main.playback_control.exo_rew

class PlayerFragment : BaseFragment() {
    override fun layoutId() = R.layout.fragment_player
    override fun tag(): String = this::class.java.simpleName

    private var isBuffering = false
    private var isExpanded = false

    private var playerSheetBehavior: BottomSheetBehavior<View>? = null

    fun setBehavior(playerSheetBehavior: BottomSheetBehavior<View>) {
        this.playerSheetBehavior = playerSheetBehavior
        this.playerSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
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
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showMinimize(false)
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

    private fun doOnPlayer(action: String) {
        MediaPlayerServiceHelper.sendIntent(requireContext(), action)
    }

    private var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent != null) {

                when (intent.action) {
                    ACTION_EPISODE -> {
                        val episode = intent.getParcelableExtra<Episode>(ACTION_EPISODE_GET)
                        openMiniPlayer(episode)
                    }
                    ACTION_PLAYER -> {
                        val action = intent.getIntExtra(ACTION_PLAYER_STATUS, MediaPlayerState.STATE_IDLE)
                        when (action) {
                            MediaPlayerState.STATE_CONNECTING -> {
                                isBuffering = true
                                playPause.setImageResource(R.drawable.ic_play)
                                if (!isExpanded)
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
