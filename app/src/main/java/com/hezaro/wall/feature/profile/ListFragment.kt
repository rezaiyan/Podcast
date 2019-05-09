package com.hezaro.wall.feature.profile

import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
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
import timber.log.Timber

class ListFragment : BaseFragment() {
    override fun layoutId() = R.layout.fragment_list
    override fun tag(): String = this::class.java.simpleName
    private val activity: MainActivity by lazy { requireActivity() as MainActivity }

    private val vm: ProfileViewModel by inject()
    public var playlistCreated = false

    companion object {
        fun getInstance() = ListFragment()
    }

    override fun onAttach(context: Context?) {
        Timber.tag(tag()).i("onAttach")
        super.onAttach(context)
    }

    override fun onAttachFragment(childFragment: Fragment?) {
        Timber.tag(tag()).i("onAttachFragment")
        super.onAttachFragment(childFragment)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        Timber.tag(tag()).i("onHiddenChanged")
        super.onHiddenChanged(hidden)
    }

    override fun onBackPressed() {
        Timber.tag(tag()).i("onBackPressed")
        super.onBackPressed()
    }

    override fun onResume() {
        Timber.tag(tag()).i("onResume")
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerList.apply {
            layoutManager = EndlessLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = EpisodeAdapter(mutableListOf()) { e, _ ->
                playlistCreated = true
                activity.prepareAndPlayPlaylist(
                    Playlist(ArrayList((recyclerList.adapter as EpisodeAdapter).episodes)),
                    e
                )

                activity.playEpisode(e)
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