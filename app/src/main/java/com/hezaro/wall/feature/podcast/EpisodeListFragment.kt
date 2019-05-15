package com.hezaro.wall.feature.podcast

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import androidx.core.view.marginBottom
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
import com.hezaro.wall.sdk.platform.utils.PARAM_EPISODE_LIST
import com.hezaro.wall.utils.EndlessLayoutManager
import kotlinx.android.synthetic.main.fragment_list.parentLayout
import kotlinx.android.synthetic.main.fragment_list.recyclerList

class EpisodeListFragment : BaseFragment() {
    override fun layoutId() = R.layout.fragment_list
    override fun tag(): String = this::class.java.simpleName
    private val activity: MainActivity by lazy { requireActivity() as MainActivity }
    private lateinit var sharedVm: SharedViewModel

    companion object {
        fun getInstance(episodes: ArrayList<Episode>) = EpisodeListFragment().also {
            it.arguments = Bundle().apply {
                putParcelableArrayList(PARAM_EPISODE_LIST, episodes)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedVm = ViewModelProviders.of(requireActivity()).get(SharedViewModel::class.java)
        sharedVm.episode.observe(this@EpisodeListFragment, Observer {
            if (it.first == UPDATE_VIEW)
                (recyclerList.adapter as EpisodeAdapter).updateRow(it.second)
        })

        val listMargin = resources.getDimension(R.dimen.mini_player_height).toInt()
        val episodes = arguments?.getParcelableArrayList<Episode>(PARAM_EPISODE_LIST)
        recyclerList.apply {
            layoutManager = EndlessLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = EpisodeAdapter(episodes!!, true) { e, _ ->
                sharedVm.isLoadedSingleEpisode(true)
                sharedVm.resetPlaylist(true)
                activity.prepareAndPlayPlaylist((recyclerList.adapter as EpisodeAdapter).episodes, e)
                listMargin(listMargin)
            }
        }

        sharedVm.listMargin.observe(this, Observer { listMargin(it) })
    }

    private fun listMargin(i: Int = 0) {
        var margin = parentLayout.marginBottom
        if (margin == 0) {
            val animator =
                ValueAnimator.ofInt(margin, i)
            animator.addUpdateListener { valueAnimator ->
                margin = valueAnimator.animatedValue as Int
                parentLayout?.requestLayout()
            }
            animator.duration = 100
            animator.start()
        }
    }
}