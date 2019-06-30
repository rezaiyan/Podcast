package com.hezaro.wall.feature.podcast

import android.animation.ValueAnimator
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.FrameLayout
import androidx.core.app.ShareCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hezaro.wall.R
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Podcast
import com.hezaro.wall.feature.adapter.EpisodeAdapter
import com.hezaro.wall.feature.main.MainActivity
import com.hezaro.wall.feature.main.SharedViewModel
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.sdk.platform.ext.load
import com.hezaro.wall.sdk.platform.ext.show
import com.hezaro.wall.sdk.platform.utils.PARAM_PODCAST
import com.hezaro.wall.utils.BlurTransformation
import com.hezaro.wall.utils.CircleTransform
import com.hezaro.wall.utils.PODCAST
import kotlinx.android.synthetic.main.fragment_list.emptyTitleView
import kotlinx.android.synthetic.main.fragment_list.recyclerList
import kotlinx.android.synthetic.main.fragment_podcast.episodeCount
import kotlinx.android.synthetic.main.fragment_podcast.podcastCover
import kotlinx.android.synthetic.main.fragment_podcast.podcastCoverBlur
import kotlinx.android.synthetic.main.fragment_podcast.podcastDescription
import kotlinx.android.synthetic.main.fragment_podcast.podcastTitle
import kotlinx.android.synthetic.main.fragment_podcast.podcasterName
import kotlinx.android.synthetic.main.fragment_podcast.share
import org.koin.androidx.viewmodel.ext.android.viewModel

class PodcastFragment : BaseFragment() {

    private lateinit var sharedVm: SharedViewModel
    private val vm: PodcastViewModel by viewModel()
    private var eCount = 0

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
        eCount = podcast?.episodeCount!!


        share.setOnClickListener {
            val shareIntent = ShareCompat.IntentBuilder.from(activity)
                .setType("text/plain")
                .setChooserTitle("ارسال  پادکست ${podcast.title} ")
                .setText("http://wall.hezaro.com/p/${podcast.id}/")
                .intent
            if (shareIntent.resolveActivity(context!!.packageManager) != null) {
                startActivity(shareIntent)
            }
        }

        podcast.let {
            podcastTitle.text = it.title
            podcasterName.text = it.creator
            episodeCount.text = "$eCount اپیزود"
            podcastCover.load(it.cover, transformation = CircleTransform())
            podcastCoverBlur.load(it.cover, transformation = BlurTransformation(requireContext()))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                podcastDescription.text = Html.fromHtml(it.description, Html.FROM_HTML_MODE_LEGACY)
            } else {
                podcastDescription.text = Html.fromHtml(it.description)
            }
            podcastDescription.movementMethod = LinkMovementMethod.getInstance()

        }

        recyclerList.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = EpisodeAdapter(
                isDownloadList = true,
                onItemClick = { e, _ ->
                    sharedVm.isPlaying(true)
                    sharedVm.resetPlaylist(true)
                    (activity as MainActivity).prepareAndPlayPlaylist(
                        (recyclerList.adapter as EpisodeAdapter).getEpisodeList(),
                        e
                    )
                },
                longClickListener = { it, _ -> (activity as MainActivity).openPodcastInfo(it) }
            )
        }

        with(vm) {
            observe(episodes, ::onSuccess)
            failure(failure, ::onFailure)
            observe(progress, ::onProgress)
            getEpisodes(podcast.id)
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
            emptyTitleView.text = getString(R.string.error_to_get_episodes)
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