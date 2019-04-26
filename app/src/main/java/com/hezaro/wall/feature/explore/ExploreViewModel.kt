package com.hezaro.wall.feature.explore

import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.domain.ExploreRepository
import com.hezaro.wall.sdk.platform.BaseViewModel
import kotlinx.coroutines.launch

class ExploreViewModel(private val repository: ExploreRepository) :
    BaseViewModel<MutableList<Episode>>() {

    fun explore(page: Int = 1, offset: Int = 20) = launch(job) {
        isExecute = true
        repository.explore(page, offset).either(::onFailure, ::onSuccess)
    }
}