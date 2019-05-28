package com.hezaro.wall.feature.profile

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.hezaro.wall.R
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.feature.adapter.EpisodeAdapter
import com.hezaro.wall.feature.main.MainActivity
import com.hezaro.wall.feature.main.SharedViewModel
import com.hezaro.wall.feature.search.UPDATE_VIEW
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.sdk.platform.ext.hide
import com.hezaro.wall.sdk.platform.ext.show
import com.hezaro.wall.sdk.platform.utils.SAVE_INSTANCE_EPISODES
import com.hezaro.wall.utils.DOWNLOAD
import com.hezaro.wall.utils.EndlessLayoutManager
import kotlinx.android.synthetic.main.fragment_list.emptyTitleView
import kotlinx.android.synthetic.main.fragment_list.recyclerList
import org.koin.android.ext.android.inject

class DownloadFragment : BaseFragment() {
    override fun layoutId() = R.layout.fragment_list
    override fun tag(): String = this::class.java.simpleName
    override fun id() = DOWNLOAD
    private val activity: MainActivity by lazy { requireActivity() as MainActivity }
    private lateinit var sharedVm: SharedViewModel
    private val vm: ProfileViewModel by inject()

    companion object {
        fun getInstance() = DownloadFragment()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.apply {
            putParcelableArrayList(SAVE_INSTANCE_EPISODES, (recyclerList?.adapter as EpisodeAdapter).episodes)
        }
        super.onSaveInstanceState(outState)
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
            adapter = EpisodeAdapter(
                isDownloadList = true,
                onItemClick = { e, _ ->
                    sharedVm.isPlaying(true)
                    sharedVm.resetPlaylist(true)
                    activity.prepareAndPlayPlaylist((recyclerList.adapter as EpisodeAdapter).episodes, e)
                },
                longClickListener = { activity.openPodcastInfo(it) })
        }
        with(vm) {
            observe(downloadEpisodes, ::onLoadEpisodes)
        }
        sharedVm.listMargin.observe(this, Observer { updateMarginList(it) })

        savedInstanceState?.let {
            val episodes = savedInstanceState.getParcelableArrayList<Episode>(SAVE_INSTANCE_EPISODES)
            if (episodes != null && episodes.isNotEmpty())
                (recyclerList.adapter as EpisodeAdapter).clearAndAddEpisode(episodes)
            else
                vm.getDownloads()
        } ?: run {
            vm.getDownloads()
        }
    }

    private fun onLoadEpisodes(it: ArrayList<Episode>) {
        if (recyclerList.adapter?.itemCount != it.size) {
            (recyclerList.adapter as EpisodeAdapter).clearAndAddEpisode(it)
        }
        if (recyclerList.adapter!!.itemCount > 0)
            emptyTitleView.hide()
        else emptyTitleView.show()
    }

    private fun updateMarginList(i: Int = -1) {
        val params = recyclerList.layoutParams as FrameLayout.LayoutParams
        if (params.bottomMargin == 0 || i >= 0) {
            val animator =
                ValueAnimator.ofInt(
                    params.bottomMargin,
                    if (i == 0) 0 else resources.getDimension(R.dimen.mini_player_height).toInt()
                )
            animator.addUpdateListener { valueAnimator ->
                params.bottomMargin = valueAnimator.animatedValue as Int
                recyclerList?.requestLayout()
            }
            animator.duration = 100
            animator.start()
        }
    }
}