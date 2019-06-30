package com.hezaro.wall.feature.search

import androidx.lifecycle.MutableLiveData
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.domain.SearchRepository
import com.hezaro.wall.sdk.platform.BaseViewModel
import kotlinx.coroutines.launch

class SearchViewModel(private val repository: SearchRepository) : BaseViewModel() {

    var search: MutableLiveData<ArrayList<Episode>> = MutableLiveData()

    fun doSearch(query: String) =
        launch {
            progress.postValue(true)
            if (query.isNotEmpty()) {
                repository.search(query).either(::onFailure, ::onSearch)
            }
        }

    private fun onSearch(list: ArrayList<Episode>) {
        progress.postValue(false)
        search.postValue(list)
    }
}