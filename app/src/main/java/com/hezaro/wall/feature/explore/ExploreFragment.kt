package com.hezaro.wall.feature.explore

import android.animation.ValueAnimator
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.hezaro.wall.R
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Status.Companion.BEST
import com.hezaro.wall.data.model.Status.Companion.NEWEST
import com.hezaro.wall.data.model.Status.Companion.OLDEST
import com.hezaro.wall.data.model.UserInfo
import com.hezaro.wall.feature.adapter.EpisodeAdapter
import com.hezaro.wall.feature.main.MainActivity
import com.hezaro.wall.feature.main.SharedViewModel
import com.hezaro.wall.feature.search.SELECT_FROM_PLAYLIST
import com.hezaro.wall.feature.search.UPDATE_VIEW
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.sdk.platform.ext.load
import com.hezaro.wall.utils.CircleTransform
import com.hezaro.wall.utils.EndlessLayoutManager
import com.hezaro.wall.utils.OnLoadMoreListener
import kotlinx.android.synthetic.main.fragment_explore.emptyViewLayout
import kotlinx.android.synthetic.main.fragment_explore.exploreList
import kotlinx.android.synthetic.main.fragment_explore.refreshLayout
import kotlinx.android.synthetic.main.fragment_explore.retry
import kotlinx.android.synthetic.main.fragment_explore.search
import kotlinx.android.synthetic.main.fragment_explore.sort
import org.koin.android.ext.android.inject

class ExploreFragment : BaseFragment(), (Episode, Int) -> Unit {

    private val vm: ExploreViewModel by inject()
    private lateinit var sharedVm: SharedViewModel
    private lateinit var episodeAdapter: EpisodeAdapter
    override fun layoutId() = R.layout.fragment_explore
    override fun tag(): String = this::class.java.simpleName
    private val activity: MainActivity by lazy { requireActivity() as MainActivity }
    private lateinit var avatar: ImageView
    private var isReset = false
    private var isLoadMoreAction = false

    private val sortDialog by lazy {
        val sortDialog = Dialog(context!!)
        sortDialog.setContentView(R.layout.dialog_sort)
        val radioBest = sortDialog.findViewById<RadioButton>(R.id.radioBest)
        val radioNewest = sortDialog.findViewById<RadioButton>(R.id.radioNewest)
        val radioOldest = sortDialog.findViewById<RadioButton>(R.id.radioOldest)
        radioBest.setOnClickListener {
            isReset = true
            radioBest.isChecked = true
            radioNewest.isChecked = false
            radioOldest.isChecked = false
            sortDialog.dismiss()
            sortPlaylist(BEST)
        }
        radioNewest.setOnClickListener {
            isReset = true
            radioBest.isChecked = false
            radioNewest.isChecked = true
            radioOldest.isChecked = false
            sortDialog.dismiss()
            sortPlaylist(NEWEST)
        }
        radioOldest.setOnClickListener {
            isReset = true
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
        if (isReset or sharedVm.isPlaying.value!!) {
            sharedVm.isPlaying(true)
            isReset = false
            activity.prepareAndPlayPlaylist(episodeAdapter.episodes, e)
        } else {
            sharedVm.notifyEpisode(Pair(SELECT_FROM_PLAYLIST, e))
        }
    }

    override fun onDestroyView() {
        vm.explore.postValue(episodeAdapter.episodes)
        vm.page = exploreList.page
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        exploreList.page = vm.page
        sharedVm = ViewModelProviders.of(requireActivity()).get(SharedViewModel::class.java)
        sharedVm.listMargin.observe(this@ExploreFragment, Observer { updateMarginList(it) })
        sharedVm.resetPlaylist.observe(this@ExploreFragment, Observer { isReset = it })
        sharedVm.userInfo.observe(this@ExploreFragment, Observer { updateUserInf(it) })
        sharedVm.episode.observe(this@ExploreFragment, Observer {
            if (it.first == UPDATE_VIEW)
                episodeAdapter.updateRow(it.second)
        })

        avatar = view.findViewById(R.id.avatar)
        episodeAdapter = EpisodeAdapter(onItemClick = this@ExploreFragment)
        exploreList.apply {
            layoutManager = EndlessLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = episodeAdapter

            loadingStatus.observe(
                this@ExploreFragment,
                Observer<Boolean> { isLoading ->
                    if (isLoading) {
                        showProgress()
                    } else
                        hideProgress()
                })

            setOnLoadMoreListener(object : OnLoadMoreListener {
                override fun onLoadMore() {
                    isLoadMoreAction = true
                    vm.explore(page = page)
                    setLoading(true)
                }
            })
        }

        with(vm) {
            observe(explore, ::onSuccess)
            failure(failure, ::onFailure)
            if (explore.value == null)
                explore(exploreList.page)
            exploreList.page++
            vm.page = exploreList.page
            exploreList.setLoading(true)
        }

        retry.setOnClickListener {
            if (!vm.isExecute) {
                exploreList.page = 1
                vm.explore(page = exploreList.page)
                exploreList.page = 2
                exploreList.setLoading(true)
                episodeAdapter.episodes.clear()
                emptyViewLayout.visibility = View.GONE
            }
            refreshLayout.isRefreshing = false
            emptyViewLayout.visibility = View.GONE
        }

        refreshLayout.setColorSchemeResources(R.color.colorAccent)
        refreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(context!!, R.color.ic_controller))
        refreshLayout.setOnRefreshListener {
            retry.performClick()
        }


        sort.setOnClickListener { sortDialog.show() }
        search.setOnClickListener {
            activity.search()
        }
        avatar.setOnClickListener {
            activity.profile()
        }
    }

    private fun sortPlaylist(sortBy: String): Boolean {
        exploreList.page = 2
        exploreList.setLoading(true)
        episodeAdapter.clearAll()
        vm.explore(page = 1, sortBy = sortBy)
        return true
    }

    private fun updateMarginList(i: Int = -1) {
        val params = exploreList.layoutParams as FrameLayout.LayoutParams
        if (params.bottomMargin == 0 || i >= 0) {
            val animator =
                ValueAnimator.ofInt(
                    params.bottomMargin,
                    if (i == 0) 0 else resources.getDimension(R.dimen.mini_player_height).toInt()
                )
            animator.addUpdateListener { valueAnimator ->
                params.bottomMargin = valueAnimator.animatedValue as Int
                exploreList?.requestLayout()
            }
            animator.duration = 100
            animator.start()
        }
    }

    private fun onSuccess(episodes: ArrayList<Episode>) {
        exploreList.setLoading(false)
        val playerIsOpen = sharedVm.playerIsOpen.value?.let { sharedVm.playerIsOpen.value } ?: false

        if (!playerIsOpen && (sharedVm.isPlaying.value == null || !sharedVm.isPlaying.value!!))
            activity.retrieveLatestEpisode()

        if (!episodeAdapter.episodes.containsAll(episodes)) {
            episodeAdapter.updateList(episodes)


            if (sharedVm.isPlaying.value != null && !sharedVm.isPlaying.value!! && !isReset) {
                activity.preparePlaylist(episodes, isLoadMoreAction)
            }
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
        if (exploreList.adapter!!.itemCount == 0)
            emptyViewLayout.visibility = View.VISIBLE
    }

    private fun updateUserInf(it: UserInfo) {
        if (::avatar.isInitialized)
            avatar.load(it.avatar, transformation = CircleTransform())
    }
}


