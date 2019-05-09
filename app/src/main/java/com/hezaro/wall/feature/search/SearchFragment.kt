package com.hezaro.wall.feature.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.hezaro.wall.R
import com.hezaro.wall.data.model.Episode
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
import kotlinx.android.synthetic.main.fragment_search.searchList
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

class SearchFragment : BaseFragment() {
    override fun layoutId() = R.layout.fragment_search
    override fun tag(): String = this::class.java.simpleName
    private val vm: SearchViewModel by inject()
    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(vm) {
            observe(search, ::onSuccess)
            failure(failure, ::onFailure)
        }

        searchList.apply {
            layoutManager = EndlessLayoutManager(context!!, LinearLayoutManager.VERTICAL, false)
            adapter = EpisodeAdapter(mutableListOf()) { e, i -> }
        }
        back.setOnClickListener { activity?.onBackPressed() }
        clear.setOnClickListener {
            ((searchList.adapter as EpisodeAdapter).clearAll())
            inputSearch.setQuery("", false)
        }
        inputSearch.normalize()

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

    private fun onSuccess(it: MutableList<Episode>) = (searchList.adapter as EpisodeAdapter).addEpisode(it)

    private fun onFailure(failure: Failure) {
    }
}