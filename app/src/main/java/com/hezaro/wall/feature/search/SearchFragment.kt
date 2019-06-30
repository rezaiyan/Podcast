package com.hezaro.wall.feature.search

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.hezaro.wall.R
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.feature.adapter.EpisodeAdapter
import com.hezaro.wall.feature.main.MainActivity
import com.hezaro.wall.feature.main.SharedViewModel
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.sdk.platform.ext.hide
import com.hezaro.wall.sdk.platform.ext.normalize
import com.hezaro.wall.sdk.platform.ext.show
import com.hezaro.wall.utils.SEARCH
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import kotlinx.android.synthetic.main.fragment_list.emptyTitleView
import kotlinx.android.synthetic.main.fragment_list.recyclerList
import kotlinx.android.synthetic.main.fragment_search.back
import kotlinx.android.synthetic.main.fragment_search.clear
import kotlinx.android.synthetic.main.fragment_search.inputSearch
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

const val SELECT_SINGLE_TRACK = 1
const val PLAY_EPISOD_FROM_PLAYLIST = 2
const val UPDATE_VIEW = 3
const val RESUME_VIEW = 4
const val PLAY_SINGLE_TRACK = 5

class SearchFragment : BaseFragment() {
    private val activity: MainActivity by lazy { requireActivity() as MainActivity }

    override fun layoutId() = R.layout.fragment_search
    override fun tag(): String = this::class.java.simpleName
    override fun id() = SEARCH
    private val vm: SearchViewModel by inject()
    private lateinit var sharedVm: SharedViewModel

    companion object {

        fun newInstance() = SearchFragment()
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emptyTitleView.text = getString(R.string.not_found_episode)
        sharedVm = ViewModelProviders.of(requireActivity()).get(SharedViewModel::class.java)
        sharedVm.listMargin.observe(this@SearchFragment, Observer { updateMarginList(it) })
        sharedVm.episode.observe(this@SearchFragment, Observer {
            if (it.first == UPDATE_VIEW)
                (recyclerList.adapter as EpisodeAdapter).updateRow(it.second)
        })
        with(vm) {
            observe(search, ::onSearch)
            observe(progress, ::onProgress)
            failure(failure, ::onFailure)
        }

        recyclerList.apply {
            layoutManager = LinearLayoutManager(context!!, LinearLayoutManager.VERTICAL, false)
            adapter = EpisodeAdapter(
                onItemClick = { e, _ ->
                    sharedVm.resetPlaylist(true)
                    sharedVm.isPlaying(true)
                    updateMarginList()
                    sharedVm.notifyEpisode(Pair(PLAY_SINGLE_TRACK, e))
                },
                longClickListener = { it, _ -> activity.openPodcastInfo(it) }
            )
        }

        back.setOnClickListener { activity.onBackPressed() }
        clear.setOnClickListener {
            emptyTitleView.hide()
            ((recyclerList.adapter as EpisodeAdapter).clearAll())
            inputSearch.setQuery("", false)
        }
        inputSearch.normalize(R.font.traffic)

        Observable.create(ObservableOnSubscribe<String> { subscriber ->
            inputSearch.setOnQueryTextListener(object : OnQueryTextListener {
                override fun onQueryTextChange(newText: String?): Boolean {
                    subscriber.onNext(newText!!)
                    return false
                }

                override fun onQueryTextSubmit(query: String?): Boolean {
                    subscriber.onNext(query!!)
                    return false
                }
            })
        })
            .map { text -> text.toLowerCase().trim() }
            .debounce(250, TimeUnit.MILLISECONDS)
            .distinct()
            .filter { text -> text.length > 2 }
            .subscribe { text ->
                emptyTitleView.hide()
                vm.doSearch(text)
            }
    }

    private fun updateMarginList(i: Int = -1) {
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

    private fun onProgress(it: Boolean) = if (it) {
        showProgress()
    } else
        hideProgress()

    private fun onSearch(it: ArrayList<Episode>) {
        if (it.isNotEmpty())
            (recyclerList.adapter as EpisodeAdapter).clearAndAddEpisode(it)
        else {
            (recyclerList.adapter as EpisodeAdapter).clearAll()
            emptyTitleView.show()
        }
    }

    private fun onFailure(failure: Failure) {
        emptyTitleView.show()
    }
}
