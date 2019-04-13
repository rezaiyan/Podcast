package com.hezaro.wall.feature.explore

import androidx.lifecycle.MutableLiveData
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Explore
import com.hezaro.wall.domain.ExploreRepository
import com.hezaro.wall.sdk.platform.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExploreViewModel(private val repository: ExploreRepository) :
    BaseViewModel() {

    var episodes: MutableLiveData<MutableList<Episode>> = MutableLiveData()

    fun episodes() {
        launch(job) {
            repository.explore().either(::onFailure, ::onExplore)
        }
    }

    private fun onExplore(it: Explore) {
        launch(Dispatchers.Main) { episodes.value = it.response }
    }
}