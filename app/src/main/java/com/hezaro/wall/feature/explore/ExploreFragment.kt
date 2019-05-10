package com.hezaro.wall.feature.explore

import android.animation.ValueAnimator
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.RadioButton
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hezaro.wall.R
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Playlist
import com.hezaro.wall.data.model.Status.Companion.BEST
import com.hezaro.wall.data.model.Status.Companion.NEWEST
import com.hezaro.wall.data.model.Status.Companion.OLDEST
import com.hezaro.wall.feature.core.main.MainActivity
import com.hezaro.wall.feature.core.player.PlayerFragment
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.utils.EndlessLayoutManager
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
    private lateinit var episodeAdapter: EpisodeAdapter
    override fun layoutId() = R.layout.fragment_explore
    override fun tag(): String = this::class.java.simpleName
    private val activity: MainActivity by lazy { requireActivity() as MainActivity }
    private var isReset = false
    private var isLoadMoreAction = false

    private val sortDialog by lazy {
        val sortDialog = Dialog(context!!)
        sortDialog.setContentView(R.layout.dialog_sort)
        val radioBest = sortDialog.findViewById<RadioButton>(R.id.radioBest)
        val radioNewest = sortDialog.findViewById<RadioButton>(R.id.radioNewest)
        val radioOldest = sortDialog.findViewById<RadioButton>(R.id.radioOldest)
        radioBest.setOnClickListener {
            radioBest.isChecked = true
            radioNewest.isChecked = false
            radioOldest.isChecked = false
            sortDialog.dismiss()
            sortPlaylist(BEST)
        }
        radioNewest.setOnClickListener {
            radioBest.isChecked = false
            radioNewest.isChecked = true
            radioOldest.isChecked = false
            sortDialog.dismiss()
            sortPlaylist(NEWEST)
        }
        radioOldest.setOnClickListener {
            radioBest.isChecked = false
            radioNewest.isChecked = false
            radioOldest.isChecked = true
            sortDialog.dismiss()
            sortPlaylist(OLDEST)
        }
        sortDialog.findViewById<RelativeLayout>(R.id.bestLayout).setOnClickListener { radioBest.performClick() }
        sortDialog.findViewById<RelativeLayout>(R.id.newLayout).setOnClickListener { radioNewest.performClick() }
        sortDialog.findViewById<RelativeLayout>(R.id.oldLayout).setOnClickListener { radioOldest.performClick() }
        sortDialog
    }

    companion object {
        fun getInstance() = ExploreFragment()
    }

    override fun invoke(e: Episode, index: Int) {
        liftExploreList()
        if (isReset) {
            isReset = false
            activity.prepareAndPlayPlaylist(Playlist(ArrayList(episodeAdapter.episodes)), e)
        } else
            activity.playEpisode(e)
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

        episodeAdapter = EpisodeAdapter(mutableListOf(), true, this@ExploreFragment)
        exploreList.apply {
            layoutManager = EndlessLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = episodeAdapter
            loadingStatus.observeForever { isLoading ->
                if (isLoading) {
                    showProgress()
                } else
                    hideProgress()
            }
            setOnLoadMoreListener(object : OnLoadMoreListener {
                override fun onLoadMore() {
                    isLoadMoreAction = true
                    vm.explore(page = page)
                    setLoading(true)
                }
            })
        }
        refreshLayout.setColorSchemeResources(R.color.colorAccent)
        refreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(context!!, R.color.ic_controller))
        refreshLayout.setOnRefreshListener {
            with(vm) {
                if (!isExecute) {
                    explore(page = 1)
                    exploreList.page = 2
                    exploreList.setLoading(true)
                    episodeAdapter.episodes.clear()
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

        sort.setOnClickListener { sortDialog.show() }
        search.setOnClickListener {
            activity.search()
        }
        profile.setOnClickListener {
            activity.profile()
        }

        activity.updateEpisode.observeForever {
            episodeAdapter.updateRow(it)
        }
    }

    private fun sortPlaylist(sortBy: String): Boolean {
        exploreList.page = 2
        exploreList.setLoading(true)
        episodeAdapter.clearAll()
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
//        if (episodeAdapter.itemCount == episodes.size)//Means Its first response: To preparing the latest episode is played, we have to retrieve that here, because playlist must be prepared too.

    private fun onSuccess(episodes: MutableList<Episode>) {
        exploreList.setLoading(false)
        val filterEpisodes = episodes.filter { !it.source.contains("live.bbc.co.uk") }.toMutableList()
        episodeAdapter.updateList(filterEpisodes)


        if (!activity.isPlayerOpen() && !activity.lastEpisodeIsAlive)
            activity.retrieveLatestEpisode()

        if (!activity.lastEpisodeIsAlive) {
            activity.preparePlaylist(Playlist(ArrayList(filterEpisodes)), isLoadMoreAction)
        } else {
            activity.lastEpisodeIsAlive = false
            isReset = true
        }

        if (isLoadMoreAction) {
            isLoadMoreAction = false
        }
    }

    private fun onFailure(failure: Failure) {
        isLoadMoreAction = false
        exploreList.onError()
        failure.message?.let { showMessage(it) }
        exploreList.setLoading(false)
    }

    fun updateEpisodeView(episode: Episode) {
        episodeAdapter.updateRow(episode)
    }

    fun resetPlaylist(reset: Boolean) {
        isReset = reset
    }
}


