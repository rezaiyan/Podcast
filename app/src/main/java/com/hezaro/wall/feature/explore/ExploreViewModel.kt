package com.hezaro.wall.feature.explore

import androidx.lifecycle.MutableLiveData
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.domain.ExploreRepository
import com.hezaro.wall.sdk.platform.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExploreViewModel(private val repository: ExploreRepository) :
    BaseViewModel() {

    var episodes: MutableLiveData<MutableList<Episode>> = MutableLiveData()
    fun explore(page: Int = 1, offset: Int = 20) {
        launch(job) {
            isExecute = true
            repository.explore(page, offset).either(::onFailure, ::onExplore)
        }
    }

    private fun onExplore(it: MutableList<Episode>) {
        launch(Dispatchers.Main) { episodes.value = it }
    }
}