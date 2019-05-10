package com.hezaro.wall.feature.search

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.hezaro.wall.R
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Playlist
import com.hezaro.wall.data.model.Podcast
import com.hezaro.wall.feature.core.main.MainActivity
import com.hezaro.wall.feature.explore.EpisodeAdapter
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.sdk.platform.ext.normalize
import com.hezaro.wall.utils.EndlessLayoutManager
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import kotlinx.android.synthetic.main.fragment_search.back
import kotlinx.android.synthetic.main.fragment_search.clear
import kotlinx.android.synthetic.main.fragment_search.inputSearch
import kotlinx.android.synthetic.main.fragment_search.podcastList
import kotlinx.android.synthetic.main.fragment_search.searchList
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

class SearchFragment : BaseFragment() {
    private val activity: MainActivity by lazy { requireActivity() as MainActivity }

    override fun layoutId() = R.layout.fragment_search
    override fun tag(): String = this::class.java.simpleName
    private val vm: SearchViewModel by inject()
    private var playlistCreated = false
    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(vm) {
            observe(search, ::onSearch)
            observe(podcast, ::onPodcast)
            observe(progress, ::onProgress)
            failure(failure, ::onFailure)
            getPodcasts()
        }

        searchList.apply {
            layoutManager = EndlessLayoutManager(context!!, LinearLayoutManager.VERTICAL, false)
            adapter = EpisodeAdapter(mutableListOf()) { e, i ->
                playlistCreated = true
                liftList()
                activity.prepareAndPlayPlaylist(
                    Playlist(arrayListOf(e)), e
                )
            }
        }

        podcastList.apply {
            layoutManager = EndlessLayoutManager(context!!, LinearLayoutManager.HORIZONTAL, false)
            adapter = PodcastAdapter(mutableListOf()) { p, i -> activity.openPodcastInfo(p) }
        }

        back.setOnClickListener { activity.onBackPressed() }
        clear.setOnClickListener {
            ((searchList.adapter as EpisodeAdapter).clearAll())
            inputSearch.setQuery("", false)
        }
        inputSearch.normalize(R.font.trafic)

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
            .subscribe { text -> vm.doSearch(text) }
    }

    private fun liftList() {
        var margin = (searchList.layoutParams as FrameLayout.LayoutParams).bottomMargin

        if (margin == 0) {
            val animator =
                ValueAnimator.ofInt(margin, resources.getDimension(R.dimen.mini_player_height).toInt())
            animator.addUpdateListener { valueAnimator ->
                margin = valueAnimator.animatedValue as Int
                searchList.requestLayout()
            }
            animator.duration = 100
            animator.start()
        }
    }

    override fun onBackPressed() {
        activity.resetPlaylist.value = playlistCreated
        super.onBackPressed()
    }

    private fun onProgress(isProgress: Boolean) {
        if (isProgress)
            showProgress()
        else hideProgress()
    }

    private fun onPodcast(it: MutableList<Podcast>) = (podcastList.adapter as PodcastAdapter).addPodcast(it)
    private fun onSearch(it: MutableList<Episode>) = (searchList.adapter as EpisodeAdapter).clearAndAddEpisode(it)

    private fun onFailure(failure: Failure) {
    }
}