package com.hezaro.wall.feature.podcast

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
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.sdk.platform.ext.show
import com.hezaro.wall.sdk.platform.utils.PARAM_PODCAST_ID
import com.hezaro.wall.utils.EndlessLayoutManager
import com.hezaro.wall.utils.PODCAST_EPISODE
import kotlinx.android.synthetic.main.fragment_list.emptyTitleView
import kotlinx.android.synthetic.main.fragment_list.recyclerList
import org.koin.android.ext.android.inject

class EpisodeListFragment : BaseFragment() {

    private val vm: PodcastViewModel by inject()

    override fun layoutId() = R.layout.fragment_list
    override fun tag(): String = this::class.java.simpleName
    override fun id() = PODCAST_EPISODE
    private val activity: MainActivity by lazy { requireActivity() as MainActivity }
    private lateinit var sharedVm: SharedViewModel

    companion object {
        fun getInstance(podcastId: Long) = EpisodeListFragment().also {
            it.arguments = Bundle().apply {
                putLong(PARAM_PODCAST_ID, podcastId)
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

        val podcastId = arguments?.getLong(PARAM_PODCAST_ID)

        recyclerList.apply {
            layoutManager = EndlessLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = EpisodeAdapter(isDownloadList = true,
                onItemClick = { e, _ ->
                    sharedVm.isPlaying(true)
                    sharedVm.resetPlaylist(true)
                    activity.prepareAndPlayPlaylist((recyclerList.adapter as EpisodeAdapter).episodes, e)
                },
                longClickListener = { activity.openPodcastInfo(it) }
            )
        }

        with(vm) {
            observe(episodes, ::onSuccess)
            failure(failure, ::onFailure)
            observe(progress, ::onProgress)
            getEpisodes(podcastId!!)
        }

        sharedVm.listMargin.observe(this, Observer { listMargin(it) })
    }

    private fun onProgress(it: Boolean) = if (it) {
        showProgress()
    } else
        hideProgress()

    private fun onSuccess(episodes: ArrayList<Episode>) {
        (recyclerList.adapter as EpisodeAdapter).clearAndAddEpisode(episodes)
    }

    private fun onFailure(failure: Failure) {
        if (recyclerList.adapter!!.itemCount == 0) {
            emptyTitleView.show()
            emptyTitleView.text = "فرایند دریافت اپیزودها با مشکل مواجه شده است"
        }
    }

    private fun listMargin(i: Int = -1) {
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