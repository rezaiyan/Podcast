package com.hezaro.wall.feature.explore

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hezaro.wall.R
import com.hezaro.wall.R.layout
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Playlist
import com.hezaro.wall.feature.core.player.PlayerFragment
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.services.MediaPlayerServiceHelper
import kotlinx.android.synthetic.main.frg_explore.exploreList
import kotlinx.android.synthetic.main.frg_explore.refreshLayout
import org.koin.android.ext.android.inject

class ExploreFragment : BaseFragment(), (Episode, Int) -> Unit {

    private lateinit var playerSheetBehavior: BottomSheetBehavior<View>
    private lateinit var playerFragment: PlayerFragment
    private lateinit var playerSheetView: View
    private val vm: ExploreViewModel by inject()
    private lateinit var exploreAdapter: ExploreAdapter
    override fun layoutId() = layout.frg_explore

    override fun invoke(episode: Episode, index: Int) {
        MediaPlayerServiceHelper.playEpisode(requireContext(), episode)
        playerSheetBehavior.peekHeight =
            resources.getDimension(R.dimen.mini_player_height).toInt()
        playerSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        playerFragment.openMiniPlayer(episode)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        vm.episodes.value?.let {
            outState.putParcelable("episodes", Playlist(ArrayList(it)))

        }
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            it.getParcelable<Playlist>("episodes")?.let { playlist ->
                vm.episodes.value = playlist.getItems()
            }

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playerFragment = (fragmentManager?.findFragmentById(R.id.filter_fragment) as PlayerFragment?)!!
        playerSheetView = playerFragment.view!!
        playerSheetBehavior = BottomSheetBehavior.from(playerSheetView)
        playerFragment.setBehavior(playerSheetBehavior)
        exploreAdapter = ExploreAdapter(mutableListOf(), this@ExploreFragment)
        exploreList.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = exploreAdapter
        }
        refreshLayout.setOnRefreshListener { vm.episodes() }
        with(vm) {
            observe(episodes, ::onSuccess)
            failure(failure, ::onFailure)
        }
        vm.episodes()
        showProgress()
    }

    private fun onSuccess(episodes: MutableList<Episode>) {
        hideProgress()
        val nonFilterEpisodes = episodes.filter { !it.source.contains("live.bbc.co.uk") }.toMutableList()
        val playlist = Playlist(ArrayList(nonFilterEpisodes))
        exploreAdapter.addEpisode(playlist.getItems())
        MediaPlayerServiceHelper.playPlaylist(requireContext(),playlist)
    }

    private fun onFailure(failure: Failure) {
        hideProgress()
    }
}


