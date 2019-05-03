package com.hezaro.wall.feature.explore

import androidx.lifecycle.MutableLiveData
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Status.Companion.BEST
import com.hezaro.wall.data.model.Status.Companion.SortBy
import com.hezaro.wall.domain.ExploreRepository
import com.hezaro.wall.sdk.platform.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExploreViewModel(private val repository: ExploreRepository) :
    BaseViewModel() {

    val explore: MutableLiveData<MutableList<Episode>> = MutableLiveData()
    private var sort = BEST
    fun explore(page: Int = 1, offset: Int = 20, sortBy: @SortBy String = sort) = launch(job) {
        sort = sortBy
        isExecute = true
        repository.explore(page, offset, sort).either(::onFailure, ::onSuccess)
    }

    private fun onSuccess(it: MutableList<Episode>) =
        launch(Dispatchers.Main) {
            isExecute = true
            explore.value = it
        }
}