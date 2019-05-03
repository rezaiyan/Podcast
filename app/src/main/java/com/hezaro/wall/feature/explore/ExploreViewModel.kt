package com.hezaro.wall.feature.explore

import androidx.lifecycle.MutableLiveData
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.domain.ExploreRepository
import com.hezaro.wall.sdk.platform.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExploreViewModel(private val repository: ExploreRepository) :
    BaseViewModel() {

    val explore: MutableLiveData<MutableList<Episode>> = MutableLiveData()
    fun explore(page: Int = 1, offset: Int = 20) = launch(job) {
        isExecute = true
        repository.explore(page, offset).either(::onFailure, ::onSuccess)
    }

    private fun onSuccess(it: MutableList<Episode>) =
        launch(Dispatchers.Main) {
            isExecute = true
            explore.value = it
        }
}