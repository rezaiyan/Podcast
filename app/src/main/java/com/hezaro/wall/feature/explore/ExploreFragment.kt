package com.hezaro.wall.feature.explore

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.hezaro.wall.R
import com.hezaro.wall.data.model.DExplore
import com.hezaro.wall.feature.adapter.ExploreAdapter
import com.hezaro.wall.feature.main.MainActivity
import com.hezaro.wall.feature.main.SharedViewModel
import com.hezaro.wall.feature.search.PLAY_SINGLE_TRACK
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.sdk.platform.ext.hide
import com.hezaro.wall.sdk.platform.ext.load
import com.hezaro.wall.sdk.platform.ext.show
import com.hezaro.wall.utils.CircleTransform
import com.hezaro.wall.utils.EXPLORE
import kotlinx.android.synthetic.main.fragment_episodes.avatar
import kotlinx.android.synthetic.main.fragment_episodes.emptyViewLayout
import kotlinx.android.synthetic.main.fragment_episodes.search
import kotlinx.android.synthetic.main.fragment_explore.exploreContainer
import kotlinx.android.synthetic.main.fragment_explore.exploreRecyclerView
import kotlinx.android.synthetic.main.fragment_explore.loginTitle
import org.koin.android.ext.android.inject

class ExploreFragment : BaseFragment() {

    override fun layoutId() = R.layout.fragment_explore

    override fun tag(): String = this::class.java.simpleName

    override fun id() = EXPLORE
    private val activity: MainActivity by lazy { requireActivity() as MainActivity }

    private val vm: ExploreViewModel by inject()
    private lateinit var sharedVm: SharedViewModel

    companion object {
        fun getInstance() = ExploreFragment()
    }

    private fun onProgress(it: Boolean) = if (it) {
        showProgress()
    } else
        hideProgress()

    private fun updateUser() {
        val userInfo = GoogleSignIn.getLastSignedInAccount(context!!)

        userInfo?.let {
            avatar.load(it.photoUrl.toString(), transformation = CircleTransform())
            loginTitle.hide()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedVm = ViewModelProviders.of(requireActivity()).get(SharedViewModel::class.java)
        sharedVm.listMargin.observe(this@ExploreFragment, Observer { updateMarginScroller(it) })

        with(vm) {
            observe(explore, ::onSuccess)
            failure(failure, ::onFailure)
            observe(progress, ::onProgress)
            explore()
        }


        search.setOnClickListener {
            activity.search()
        }
        avatar.setOnClickListener {
            activity.profile()
        }
    }

    private fun onSuccess(it: DExplore) {
        exploreRecyclerView.adapter = ExploreAdapter(it, onEpisodeClick = { e, _ ->
            //            if (sharedVm.isPlaying.value!!) {
//                sharedVm.isPlaying(true)
//                isReset = false
//            activity.prepareAndPlayPlaylist(ArrayList(it.episodeItems[0].episodes), e)
//            } else {
            sharedVm.notifyEpisode(Pair(PLAY_SINGLE_TRACK, e))
//            }
        }, onPodcastClick = { it, _ -> activity.openPodcastInfo(it) })
    }

    private fun onFailure(it: Failure) {
        when (it) {
            is Failure.NetworkConnection -> showMessage(it.message)
        }
        exploreRecyclerView?.onError()
        it.message?.let { showMessage(it) }
        exploreRecyclerView.setLoading(false)
        if (exploreRecyclerView.adapter!!.itemCount == 0)
            emptyViewLayout.show()
    }

    private fun updateMarginScroller(i: Int = -1) {
        val params = exploreContainer.layoutParams as ConstraintLayout.LayoutParams
        if (params.bottomMargin == 0 || i >= 0) {
            val animator =
                ValueAnimator.ofInt(
                    params.bottomMargin,
                    if (i == 0) 0 else resources.getDimension(R.dimen.mini_player_height).toInt()
                )
            animator.addUpdateListener { valueAnimator ->
                params.bottomMargin = valueAnimator.animatedValue as Int
                exploreContainer?.requestLayout()
            }
            animator.duration = 100
            animator.start()
        }
    }
}