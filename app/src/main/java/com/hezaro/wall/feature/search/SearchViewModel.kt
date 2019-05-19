package com.hezaro.wall.feature.search

import androidx.lifecycle.MutableLiveData
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Podcast
import com.hezaro.wall.domain.SearchRepository
import com.hezaro.wall.sdk.platform.BaseViewModel
import kotlinx.coroutines.launch

class SearchViewModel(private val repository: SearchRepository) : BaseViewModel() {

    var search: MutableLiveData<ArrayList<Episode>> = MutableLiveData()
    var podcast: MutableLiveData<ArrayList<Podcast>> = MutableLiveData()

    fun getPodcasts() =
        launch {
            isExecute = true
            repository.podcast().either(::onFailure, ::onPodcast)
        }

    fun doSearch(query: String) =
        launch {
            progress.postValue(true)
            isExecute = true
            if (query.isNotEmpty()) {
                repository.search(query).either(::onFailure, ::onSearch)
            }
        }

    private fun onPodcast(list: ArrayList<Podcast>) {
        isExecute = false
        podcast.postValue(list)
    }

    private fun onSearch(list: ArrayList<Episode>) {
        isExecute = false
        progress.postValue(false)
        search.postValue(list)
    }
}