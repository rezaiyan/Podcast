package com.hezaro.wall.feature.podcast

import android.os.Bundle
import android.view.View
import com.hezaro.wall.R
import com.hezaro.wall.data.model.Podcast
import com.hezaro.wall.feature.core.main.MainActivity
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.sdk.platform.ext.load
import com.hezaro.wall.sdk.platform.utils.PullDismissLayout
import com.hezaro.wall.utils.PARAM_PODCAST
import kotlinx.android.synthetic.main.fragment_podcast.podcastCover
import kotlinx.android.synthetic.main.fragment_podcast.podcastTitle
import kotlinx.android.synthetic.main.fragment_podcast.podcasterName
import kotlinx.android.synthetic.main.fragment_podcast.pullLayout

class PodcastFragment : BaseFragment(), PullDismissLayout.Listener {
    private val activity: MainActivity by lazy { requireActivity() as MainActivity }

    override fun onDismissed() = activity.onBackPressed()

    override fun onShouldInterceptTouchEvent() = activity.isPlayerExpand()

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
        pullLayout.setListener(this)
        val podcast = arguments?.getParcelable<Podcast>(PARAM_PODCAST)

        podcast?.let {
            podcastTitle.text = it.title
            podcasterName.text = it.creator
            podcastCover.load(it.cover)
        }
    }
}