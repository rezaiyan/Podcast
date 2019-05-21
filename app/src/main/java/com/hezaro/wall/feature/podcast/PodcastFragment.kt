package com.hezaro.wall.feature.podcast

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.hezaro.wall.R
import com.hezaro.wall.data.model.Podcast
import com.hezaro.wall.feature.adapter.PagerAdapter
import com.hezaro.wall.feature.main.SharedViewModel
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.sdk.platform.ext.load
import com.hezaro.wall.sdk.platform.utils.PARAM_PODCAST
import com.hezaro.wall.utils.PODCAST
import kotlinx.android.synthetic.main.fragment_podcast.podcastCover
import kotlinx.android.synthetic.main.fragment_podcast.podcastDescription
import kotlinx.android.synthetic.main.fragment_podcast.podcastTitle
import kotlinx.android.synthetic.main.fragment_podcast.podcasterName
import kotlinx.android.synthetic.main.fragment_podcast.tabLayout
import kotlinx.android.synthetic.main.fragment_podcast.viewpager

class PodcastFragment : BaseFragment() {

    private lateinit var sharedVm: SharedViewModel
    private var episodeCount = 0

    override fun layoutId() = R.layout.fragment_podcast
    override fun tag(): String = this::class.java.simpleName
    override fun id() = PODCAST
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

        val podcast = arguments?.getParcelable<Podcast>(PARAM_PODCAST)
        episodeCount = podcast?.episodeCount!!
        podcast.let {
            podcastTitle.text = it.title
            podcasterName.text = it.creator
            podcastCover.load(it.cover)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                podcastDescription.text = Html.fromHtml(it.description, Html.FROM_HTML_MODE_LEGACY)
            } else {
                podcastDescription.text = Html.fromHtml(it.description)
            }
            podcastDescription.movementMethod = LinkMovementMethod.getInstance()

        }

        viewpager.adapter =
            PagerAdapter(
                childFragmentManager,
                arrayOf(EpisodeListFragment.getInstance(podcast.id)),
                arrayOf("اپیزودها ($episodeCount)")
            )
        tabLayout.setupWithViewPager(viewpager)


    }

}