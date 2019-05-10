package com.hezaro.wall.feature.profile

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import androidx.core.view.marginBottom
import androidx.recyclerview.widget.RecyclerView
import com.hezaro.wall.R
import com.hezaro.wall.data.model.Playlist
import com.hezaro.wall.feature.core.main.MainActivity
import com.hezaro.wall.feature.explore.EpisodeAdapter
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.utils.EndlessLayoutManager
import kotlinx.android.synthetic.main.fragment_list.parentLayout
import kotlinx.android.synthetic.main.fragment_list.recyclerList
import org.koin.android.ext.android.inject

class DownloadFragment : BaseFragment() {
    override fun layoutId() = R.layout.fragment_list
    override fun tag(): String = this::class.java.simpleName
    private val activity: MainActivity by lazy { requireActivity() as MainActivity }

    private val vm: ProfileViewModel by inject()
    var playlistCreated = false

    companion object {
        fun getInstance() = DownloadFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerList.apply {
            layoutManager = EndlessLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = EpisodeAdapter(mutableListOf(), true) { e, _ ->
                playlistCreated = true
                activity.prepareAndPlayPlaylist(
                    Playlist(ArrayList((recyclerList.adapter as EpisodeAdapter).episodes)),
                    e
                )
                liftList()
            }
        }

        with(vm) {
            observe(episodes) {
                if ((recyclerList.adapter as EpisodeAdapter).itemCount != it.size) {
                    (recyclerList.adapter as EpisodeAdapter).clearAndAddEpisode(it)
                }
            }
            getEpisodes()
        }

        if (activity.isPlayerOpen())
            liftList()
    }

    private fun liftList() {
        var margin = parentLayout.marginBottom
        if (margin == 0) {
            val animator =
                ValueAnimator.ofInt(margin, resources.getDimension(R.dimen.mini_player_height).toInt())
            animator.addUpdateListener { valueAnimator ->
                margin = valueAnimator.animatedValue as Int
                parentLayout.requestLayout()
            }
            animator.duration = 100
            animator.start()
        }
    }
}