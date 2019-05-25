package com.hezaro.wall.feature.explore

import androidx.lifecycle.MutableLiveData
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Status.Companion.NEWEST
import com.hezaro.wall.data.model.Status.Companion.SortBy
import com.hezaro.wall.domain.ExploreRepository
import com.hezaro.wall.sdk.platform.BaseViewModel
import kotlinx.coroutines.launch

class ExploreViewModel(private val repository: ExploreRepository) :
    BaseViewModel() {

    val explore: MutableLiveData<ArrayList<Episode>> = MutableLiveData()
    private var sort = NEWEST
    fun explore(page: Int = 1, offset: Int = 20, sortBy: @SortBy String = sort) =
        launch {
            sort = sortBy
            progress.postValue(true)
            repository.explore(page, offset, sort).either(::onFailure, ::onSuccess)
        }

    private fun onSuccess(it: ArrayList<Episode>) {
        progress.postValue(false)
        explore.postValue(it)
    }
}