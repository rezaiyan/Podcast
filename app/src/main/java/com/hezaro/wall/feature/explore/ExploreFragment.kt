package com.hezaro.wall.feature.explore

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hezaro.wall.R
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Playlist
import com.hezaro.wall.data.model.Status.Companion.BEST
import com.hezaro.wall.data.model.Status.Companion.BEST_
import com.hezaro.wall.data.model.Status.Companion.NEWEST
import com.hezaro.wall.data.model.Status.Companion.NEWEST_
import com.hezaro.wall.data.model.Status.Companion.OLDEST
import com.hezaro.wall.data.model.Status.Companion.OLDEST_
import com.hezaro.wall.data.model.Status.Companion.SortBy
import com.hezaro.wall.feature.core.main.MainActivity
import com.hezaro.wall.feature.core.player.PlayerFragment
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.services.MediaPlayerServiceHelper
import com.hezaro.wall.utils.OnLoadMoreListener
import com.hezaro.wall.utils.SAVE_INSTANCE_EPISODES
import kotlinx.android.synthetic.main.fragment_explore.exploreList
import kotlinx.android.synthetic.main.fragment_explore.refreshLayout
import kotlinx.android.synthetic.main.toolbar.profile
import kotlinx.android.synthetic.main.toolbar.search
import kotlinx.android.synthetic.main.toolbar.sort
import org.koin.android.ext.android.inject

class ExploreFragment : BaseFragment(), (Episode, Int) -> Unit {

    private lateinit var playerSheetBehavior: BottomSheetBehavior<View>
    private lateinit var playerFragment: PlayerFragment
    private val vm: ExploreViewModel by inject()
    private lateinit var exploreAdapter: ExploreAdapter
    override fun layoutId() = R.layout.fragment_explore
    override fun tag(): String = this::class.java.simpleName
    private val activity: MainActivity by lazy { requireActivity() as MainActivity }

    override fun invoke(episode: Episode, index: Int) {
        liftExploreList()
        playerFragment.openMiniPlayer(episode)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        vm.explore.value?.let {
            outState.putParcelable(SAVE_INSTANCE_EPISODES, Playlist(ArrayList(it)))

        }
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            it.getParcelable<Playlist>(SAVE_INSTANCE_EPISODES)?.let { playlist ->
                vm.explore.value = playlist.getItems()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playerFragment = (fragmentManager?.findFragmentById(R.id.playerFragment) as PlayerFragment?)!!
        playerFragment.view?.let { playerSheetBehavior = BottomSheetBehavior.from(it) }
            .also { playerFragment.setBehavior(playerSheetBehavior) }

        exploreAdapter = ExploreAdapter(mutableListOf(), this@ExploreFragment)
        exploreList.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = exploreAdapter
            loadingStatus.observeForever { isLoading ->
                if (isLoading) {
                    showProgress()
                } else
                    hideProgress()
            }
            setOnLoadMoreListener(object : OnLoadMoreListener {
                override fun onLoadMore() {
                    vm.explore(page = page)
                    setLoading(true)
                }
            })
        }

        refreshLayout.setOnRefreshListener {
            with(vm) {
                if (!isExecute) {
                    explore(page = 1)
                    exploreList.page = 2
                    exploreList.setLoading(true)
                    exploreAdapter.episodes.clear()
                }
            }
            refreshLayout.isRefreshing = false
        }

        with(vm) {
            observe(explore, ::onSuccess)
            failure(failure, ::onFailure)
            explore(exploreList.page)
            exploreList.page++
            exploreList.setLoading(true)
        }

        val menu = PopupMenu(context!!, sort)
        menu.menu.add(NEWEST_)
        menu.menu.add(OLDEST_)
        menu.menu.add(BEST_)

        sort.setOnClickListener {
            menu.show()

        }
        menu.setOnMenuItemClickListener {
            when (it.title) {
                NEWEST_ -> {
                    sortPlaylist(NEWEST)
                }
                OLDEST_ -> {
                    sortPlaylist(OLDEST)
                }
                BEST_ -> {
                    sortPlaylist(BEST)
                }
                else -> false

            }
        }
        search.setOnClickListener {
            activity.search()
        }
        profile.setOnClickListener {
            activity.profile()
        }
    }

    private fun sortPlaylist(sortBy: @SortBy String): Boolean {
        exploreList.page = 2
        exploreList.setLoading(true)
        exploreAdapter.clearAll()
        vm.explore(page = 1, sortBy = sortBy)
        return true
    }

    private fun liftExploreList() {
        val params = exploreList.layoutParams as FrameLayout.LayoutParams
        if (params.bottomMargin == 0) {
            val animator =
                ValueAnimator.ofInt(params.bottomMargin, resources.getDimension(R.dimen.mini_player_height).toInt())
            animator.addUpdateListener { valueAnimator ->
                params.bottomMargin = valueAnimator.animatedValue as Int
                exploreList.requestLayout()
            }
            animator.duration = 100
            animator.start()
        }
    }

    private fun onSuccess(episodes: MutableList<Episode>) {
        exploreList.setLoading(false)
        val nonFilterEpisodes = episodes.filter { !it.source.contains("live.bbc.co.uk") }.toMutableList()
        val playlist = Playlist(ArrayList(nonFilterEpisodes))
        exploreAdapter.addEpisode(playlist.getItems())
        MediaPlayerServiceHelper.playPlaylist(requireContext(), playlist)
    }

    private fun onFailure(failure: Failure) {
        exploreList.onError()
        failure.message?.let { showMessage(it) }
        exploreList.setLoading(false)
    }
}


