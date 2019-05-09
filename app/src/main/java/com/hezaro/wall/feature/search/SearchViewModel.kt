package com.hezaro.wall.feature.search

import androidx.lifecycle.MutableLiveData
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.domain.SearchRepository
import com.hezaro.wall.sdk.platform.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchViewModel(private val repository: SearchRepository) : BaseViewModel() {

    var search: MutableLiveData<MutableList<Episode>> = MutableLiveData()

    fun doSearch(query: String) = launch(job) {
        isExecute = true
        if (query.isNotEmpty())
            repository.search(query).either(::onFailure, ::onSearch)
    }

    private fun onSearch(list: MutableList<Episode>) {
        isExecute = false
        launch(Dispatchers.Main) { search.value = list }
    }
}