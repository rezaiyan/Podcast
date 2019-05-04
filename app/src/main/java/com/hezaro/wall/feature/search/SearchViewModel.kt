package com.hezaro.wall.feature.search

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.hezaro.wall.sdk.platform.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchViewModel : BaseViewModel() {

    var search: MutableLiveData<String> = MutableLiveData()

    fun doSearch(query: String) = launch(job) {
        Log.i("tagggg", query)
        isExecute = true
//        if (query.isNotEmpty())
//        repository.search().either(::onFailure, ::onVersion)
    }

    private fun onSearch(it: String) {
        isExecute = false
        launch(Dispatchers.Main) { search.value = it }
    }
}