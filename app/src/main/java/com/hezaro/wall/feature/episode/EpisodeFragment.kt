package com.hezaro.wall.feature.episode

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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.hezaro.wall.R
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.feature.main.MainActivity
import com.hezaro.wall.feature.main.SharedViewModel
import com.hezaro.wall.feature.search.UPDATE_VIEW
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.sdk.platform.ext.load
import com.hezaro.wall.sdk.platform.utils.PARAM_EPISODE
import com.hezaro.wall.utils.EPISODE
import kotlinx.android.synthetic.main.fragment_episode.*
import org.koin.android.ext.android.inject

class EpisodeFragment : BaseFragment() {

    override fun layoutId() = R.layout.fragment_episode
    override fun tag(): String = this::class.java.simpleName
    override fun id() = EPISODE
    private val activity: MainActivity by lazy { requireActivity() as MainActivity }

    private val vm: EpisodeViewModel by inject()
    private lateinit var sharedVm: SharedViewModel
    private var currentEpisode: Episode? = null

    companion object {
        fun newInstance(episode: Episode) = EpisodeFragment().also {
            it.arguments = Bundle().apply {
                putParcelable(PARAM_EPISODE, episode)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedVm = ViewModelProviders.of(requireActivity()).get(SharedViewModel::class.java)
        currentEpisode = arguments?.getParcelable(PARAM_EPISODE)
        sharedVm.episode.observe(this, Observer {
            currentEpisode = it.second
            updateView()
        })
        sharedVm.listMargin.observe(this, Observer { updateMarginScroller(it) })

        updateView()
        podcastTitle.setOnClickListener { activity.openPodcastInfo(currentEpisode!!.podcast) }
        bookmarkStatus.visibility =
            if (GoogleSignIn.getLastSignedInAccount(context) != null) View.VISIBLE else View.INVISIBLE

        share.setOnClickListener {
            val shareIntent = ShareCompat.IntentBuilder.from(activity)
                .setType("text/plain")
                .setChooserTitle("ارسال اپیزود ${currentEpisode?.title} ")
                .setText("http://wall.hezaro.com/e/${currentEpisode?.id}/")
                .intent
            if (shareIntent.resolveActivity(context!!.packageManager) != null) {
                startActivity(shareIntent)
            }
        }
        bookmarkStatus.setOnClickListener {

            if (!currentEpisode!!.isBookmarked) {
                bookmarkStatus.setMinAndMaxFrame(0, 50)
                bookmarkStatus.speed = 1.0F
                bookmarkStatus.playAnimation()
            } else {
                bookmarkStatus.setMinAndMaxFrame(0, 50)
                bookmarkStatus.speed = -1.0F
                bookmarkStatus.playAnimation()
            }
            vm.sendBookmarkAction(!currentEpisode!!.isBookmarked, currentEpisode!!.id)
            currentEpisode!!.isBookmarked = !currentEpisode!!.isBookmarked
            sharedVm.notifyEpisode(Pair(UPDATE_VIEW, currentEpisode!!))
        }
    }

    private fun updateMarginScroller(i: Int = -1) {
        val params = description.layoutParams as FrameLayout.LayoutParams
        if (params.bottomMargin == 0 || i >= 0) {
            val animator =
                ValueAnimator.ofInt(
                    params.bottomMargin,
                    if (i == 0) 0 else resources.getDimension(R.dimen.mini_player_height).toInt()
                )
            animator.addUpdateListener { valueAnimator ->
                params.bottomMargin = valueAnimator.animatedValue as Int
                description?.requestLayout()
            }
            animator.duration = 100
            animator.start()
        }
    }

    private fun updateView() {

        currentEpisode?.let {
            bookmarkStatus.visibility = if (vm.userIsLogin()) View.VISIBLE else View.INVISIBLE
            if (it.isBookmarked)
                bookmarkStatus.frame = 50
            else bookmarkStatus.frame = 0

            podcastCover.load(it.podcast.cover)
            episodeCover.load(it.cover)
            episodeTitle.text = it.title
            podcastTitle.text = it.podcast.title
            playedCount.text = it.getView()
            likeCount.text = it.getLike()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                description.text = Html.fromHtml(it.description, Html.FROM_HTML_MODE_LEGACY)
            } else {
                description.text = Html.fromHtml(it.description)
            }
            description.movementMethod = LinkMovementMethod.getInstance()

        }
    }
}