package com.hezaro.wall.feature.episodes

import android.animation.ValueAnimator
import android.os.Bundle
import android.widget.FrameLayout
import com.hezaro.wall.R
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Status.Companion.NEWEST
import com.hezaro.wall.feature.adapter.EpisodeAdapter
import com.hezaro.wall.feature.main.MainActivity
import com.hezaro.wall.feature.main.SharedViewModel
import com.hezaro.wall.feature.search.PLAY_EPISOD_FROM_PLAYLIST
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.sdk.platform.ext.hide
import com.hezaro.wall.sdk.platform.ext.show
import com.hezaro.wall.sdk.platform.utils.SAVE_INSTANCE_EPISODES
import com.hezaro.wall.sdk.platform.utils.SAVE_INSTANCE_PAGE
import com.hezaro.wall.sdk.platform.utils.SAVE_INSTANCE_SORT
import com.hezaro.wall.utils.EPISODES
import kotlinx.android.synthetic.main.fragment_episodes.emptyViewLayout
import kotlinx.android.synthetic.main.fragment_episodes.episodeList
import org.koin.android.ext.android.inject

class EpisodesFragment : BaseFragment() {

    private val vm: EpisodesViewModel by inject()
    private val activity: MainActivity by lazy { requireActivity() as MainActivity }
    private var isReset = false
    private var isLoadMoreAction = false
    private var page = 1
    private lateinit var sharedVm: SharedViewModel
    override fun layoutId() = R.layout.fragment_episodes
    override fun tag(): String = this::class.java.simpleName
    override fun id() = EPISODES

    private val episodeAdapter: EpisodeAdapter by lazy {
        EpisodeAdapter(
            onItemClick = { e, _ ->
                if (isReset or sharedVm.isPlaying.value!!) {
                    sharedVm.isPlaying(true)
                    isReset = false
                    activity.prepareAndPlayPlaylist(episodeAdapter.getEpisodeList(), e)
                } else {
                    sharedVm.notifyEpisode(Pair(PLAY_EPISOD_FROM_PLAYLIST, e))
                }
            },
            longClickListener = { it, _ -> activity.openPodcastInfo(it) }
        )
    }

    companion object {
        fun getInstance(sort: String) = EpisodesFragment().apply {
            arguments = Bundle().also {
                it.putString(SAVE_INSTANCE_SORT, sort)
            }
        }
    }

    override fun onStop() {
        episodeList.removeOnLoadMoreListener()
        page = episodeList.page
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (episodeAdapter.episodes.size > 0)
            outState.apply {
                putParcelableArrayList(SAVE_INSTANCE_EPISODES, episodeAdapter.getEpisodeList())
                putInt(SAVE_INSTANCE_PAGE, page)
                putString(SAVE_INSTANCE_SORT, arguments?.getString(SAVE_INSTANCE_SORT, NEWEST))
            }
        super.onSaveInstanceState(outState)
    }

    private fun onProgress(it: Boolean) = if (it) {
        showProgress()
    } else
        hideProgress()

    private fun sortPlaylist(): Boolean {
        episodeList.page = 2
        episodeList.setLoading(true)
        episodeAdapter.clearAll()
        vm.getEpisodes(page = 1)
        return true
    }

    private fun updateMarginList(i: Int = -1) {
        val params = episodeList.layoutParams as FrameLayout.LayoutParams
        if (params.bottomMargin == 0 || i >= 0) {
            val animator =
                ValueAnimator.ofInt(
                    params.bottomMargin,
                    if (i == 0) 0 else resources.getDimension(R.dimen.mini_player_height).toInt()
                )
            animator.addUpdateListener { valueAnimator ->
                params.bottomMargin = valueAnimator.animatedValue as Int
                episodeList?.requestLayout()
            }
            animator.duration = 100
            animator.start()
        }
    }

    private fun onSuccess(episodes: ArrayList<Episode>) {
        emptyViewLayout.hide()
        episodeList.setLoading(false)

        val playerIsOpen = sharedVm.playerIsOpen.value?.let { sharedVm.playerIsOpen.value } ?: false

        if (!playerIsOpen && (sharedVm.isPlaying.value == null || !sharedVm.isPlaying.value!!))
            activity.retrieveLatestEpisode()

        if (!episodeAdapter.episodes.containsAll(episodes)) {
            episodeAdapter.updateList(episodes)


            if (sharedVm.isPlaying.value != null && !sharedVm.isPlaying.value!! && !isReset) {
                activity.preparePlaylist(episodes, isLoadMoreAction)
            }
        }

        if (isLoadMoreAction) {
            isLoadMoreAction = false
        }
    }

    private fun onFailure(failure: Failure) {
        when (failure) {
            is Failure.NetworkConnection -> showMessage(failure.message)
        }
        isLoadMoreAction = false
        episodeList.onError()
        failure.message?.let { showMessage(it) }
        episodeList.setLoading(false)
        if (episodeList.adapter!!.itemCount == 0)
            emptyViewLayout.show()
    }
}


