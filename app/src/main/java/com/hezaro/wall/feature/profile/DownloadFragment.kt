package com.hezaro.wall.feature.profile

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import androidx.core.view.marginBottom
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.hezaro.wall.R
import com.hezaro.wall.feature.adapter.EpisodeAdapter
import com.hezaro.wall.feature.main.MainActivity
import com.hezaro.wall.feature.main.SharedViewModel
import com.hezaro.wall.feature.search.UPDATE_VIEW
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.utils.EndlessLayoutManager
import kotlinx.android.synthetic.main.fragment_list.parentLayout
import kotlinx.android.synthetic.main.fragment_list.recyclerList
import org.koin.android.ext.android.inject

class DownloadFragment : BaseFragment() {
    override fun layoutId() = R.layout.fragment_list
    override fun tag(): String = this::class.java.simpleName
    private val activity: MainActivity by lazy { requireActivity() as MainActivity }
    private lateinit var sharedVm: SharedViewModel
    private val vm: ProfileViewModel by inject()

    companion object {
        fun getInstance() = DownloadFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedVm = ViewModelProviders.of(requireActivity()).get(SharedViewModel::class.java)
        sharedVm.episode.observe(this@DownloadFragment, Observer {
            if (it.first == UPDATE_VIEW)
                (recyclerList.adapter as EpisodeAdapter).updateRow(it.second)
        })

        recyclerList.apply {
            layoutManager = EndlessLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = EpisodeAdapter(isDownloadList = true) { e, _ ->
                sharedVm.isPlaying(true)
                sharedVm.resetPlaylist(true)
                activity.prepareAndPlayPlaylist((recyclerList.adapter as EpisodeAdapter).episodes, e)
                liftList()
            }
        }

        with(vm) {
            observe(episodes) {
                if ((recyclerList.adapter as EpisodeAdapter).itemCount != it.size) {
                    sharedVm.setDownloadSize(it.size)
                    (recyclerList.adapter as EpisodeAdapter).clearAndAddEpisode(it)
                }
            }
            getEpisodes()
        }

        sharedVm.listMargin.observe(this, Observer { liftList(it) })
    }

    private fun liftList(i: Int = -1) {
        var margin = parentLayout.marginBottom
        if (margin == 0) {
            val animator =
                ValueAnimator.ofInt(
                    margin,
                    if (i == 0) 0 else resources.getDimension(R.dimen.mini_player_height).toInt()
                )
            animator.addUpdateListener { valueAnimator ->
                margin = valueAnimator.animatedValue as Int
                parentLayout?.requestLayout()
            }
            animator.duration = 100
            animator.start()
        }
    }
}