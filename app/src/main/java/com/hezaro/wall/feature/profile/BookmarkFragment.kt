package com.hezaro.wall.feature.profile

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hezaro.wall.R
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.feature.adapter.EpisodeAdapter
import com.hezaro.wall.feature.main.MainActivity
import com.hezaro.wall.feature.main.SharedViewModel
import com.hezaro.wall.feature.search.UPDATE_VIEW
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.sdk.platform.ext.hide
import com.hezaro.wall.sdk.platform.ext.show
import com.hezaro.wall.sdk.platform.utils.SAVE_INSTANCE_EPISODES
import com.hezaro.wall.utils.BOOKMARK
import kotlinx.android.synthetic.main.fragment_list.emptyTitleView
import kotlinx.android.synthetic.main.fragment_list.recyclerList
import org.koin.android.ext.android.inject

class BookmarkFragment : BaseFragment() {
    override fun layoutId() = R.layout.fragment_list
    override fun tag(): String = this::class.java.simpleName
    override fun id() = BOOKMARK
    private val activity: MainActivity by lazy { requireActivity() as MainActivity }
    private lateinit var sharedVm: SharedViewModel
    private val vm: ProfileViewModel by inject()

    companion object {
        fun getInstance() = BookmarkFragment()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.apply {
            putParcelableArrayList(SAVE_INSTANCE_EPISODES, (recyclerList.adapter as EpisodeAdapter).getEpisodeList())
        }
        super.onSaveInstanceState(outState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emptyTitleView.text = getString(R.string.is_not_bookmark_episode)
        sharedVm = ViewModelProviders.of(requireActivity()).get(SharedViewModel::class.java)
        sharedVm.listMargin.observe(this, Observer { updateMarginList(it) })
        sharedVm.episode.observe(this@BookmarkFragment, Observer {
            if (it.first == UPDATE_VIEW)
                (recyclerList.adapter as EpisodeAdapter).updateRow(it.second)
        })

        recyclerList.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = EpisodeAdapter(
                isBookmarkList = true,
                onItemClick = { e, _ ->
                    sharedVm.isPlaying(true)
                    sharedVm.resetPlaylist(true)
                    activity.prepareAndPlayPlaylist((recyclerList.adapter as EpisodeAdapter).getEpisodeList(), e)
                },
                longClickListener = { it, _ -> activity.openPodcastInfo(it) })
        }
        with(vm) {
            observe(bookmarkEpisodes, ::onLoadEpisodes)
            observe(failure, ::onFailure)
            observe(progress, ::onProgress)
        }

        savedInstanceState?.let {
            val episodes = savedInstanceState.getParcelableArrayList<Episode>(SAVE_INSTANCE_EPISODES)
            if (!episodes.isNullOrEmpty())
                (recyclerList.adapter as EpisodeAdapter).clearAndAddEpisode(episodes)
            else
                vm.getBookmarks()
        } ?: run {
            vm.getBookmarks()
        }
    }

    private fun onProgress(it: Boolean) = if (it) {
        showProgress()
    } else
        hideProgress()

    private fun onFailure(failure: Failure) {
        emptyTitleView.apply {
            text = getString(R.string.error_to_get_bookmarks)
            show()
        }
    }

    private fun onLoadEpisodes(it: ArrayList<Episode>) {
        if (it.size > 0) {
            it.reverse()
            emptyTitleView.hide()
            (recyclerList.adapter as EpisodeAdapter).clearAndAddEpisode(it)
        } else emptyTitleView.show()
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