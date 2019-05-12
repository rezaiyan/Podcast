package com.hezaro.wall.feature.podcast

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hezaro.wall.R
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Podcast
import com.hezaro.wall.feature.adapter.PagerAdapter
import com.hezaro.wall.feature.main.MainActivity
import com.hezaro.wall.feature.main.SharedViewModel
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.sdk.platform.ext.load
import com.hezaro.wall.sdk.platform.utils.PARAM_PODCAST
import com.hezaro.wall.sdk.platform.utils.PullDismissLayout
import kotlinx.android.synthetic.main.fragment_podcast.podcastCover
import kotlinx.android.synthetic.main.fragment_podcast.podcastTitle
import kotlinx.android.synthetic.main.fragment_podcast.podcasterName
import kotlinx.android.synthetic.main.fragment_podcast.pullLayout
import kotlinx.android.synthetic.main.fragment_podcast.tabLayout
import kotlinx.android.synthetic.main.fragment_podcast.viewpager
import org.koin.android.ext.android.inject

class PodcastFragment : BaseFragment(), PullDismissLayout.Listener {
    private val activity: MainActivity by lazy { requireActivity() as MainActivity }

    private val vm: PodcastViewModel by inject()
    private lateinit var sharedVm: SharedViewModel
    private var episodeCount = 0
    override fun onDismissed() = activity.onBackPressed()

    override fun onShouldInterceptTouchEvent() = sharedVm.sheetState.value == BottomSheetBehavior.STATE_EXPANDED

    override fun layoutId() = R.layout.fragment_podcast
    override fun tag(): String = this::class.java.simpleName

    companion object {
        fun newInstance(podcast: Podcast) = PodcastFragment().also {
            it.arguments = Bundle().apply {
                putParcelable(PARAM_PODCAST, podcast)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedVm = ViewModelProviders.of(requireActivity()).get(SharedViewModel::class.java)

        pullLayout.setListener(this)
        val podcast = arguments?.getParcelable<Podcast>(PARAM_PODCAST)
        episodeCount = podcast?.episodeCount!!
        podcast.let {
            podcastTitle.text = it.title
            podcasterName.text = it.creator
            podcastCover.load(it.cover)
        }

        with(vm) {
            observe(episodes, ::onSuccess)
            failure(failure, ::onFailure)
            getEpisodes(podcast.id)
        }
    }

    private fun onSuccess(episodes: ArrayList<Episode>) {
        viewpager.adapter =
            PagerAdapter(
                childFragmentManager,
                arrayOf(EpisodeListFragment.getInstance(ArrayList(episodes))),
                arrayOf("اپیزودها ($episodeCount)")
            )
        tabLayout.setupWithViewPager(viewpager)
    }

    private fun onFailure(failure: Failure) {
    }
}