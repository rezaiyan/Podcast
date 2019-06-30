package com.hezaro.wall.feature.episodes

import androidx.lifecycle.MutableLiveData
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Status.Companion.NEWEST
import com.hezaro.wall.data.model.Status.Companion.SortBy
import com.hezaro.wall.domain.EpisodesRepository
import com.hezaro.wall.sdk.platform.BaseViewModel
import kotlinx.coroutines.launch

class EpisodesViewModel(private val repository: EpisodesRepository) :
    BaseViewModel() {

    val episodes: MutableLiveData<ArrayList<Episode>> = MutableLiveData()
    private var sort = NEWEST
    fun getEpisodes(page: Int = 1, offset: Int = 20, sortBy: @SortBy String = sort) =
        launch {
            sort = sortBy
            progress.postValue(true)
            repository.episodes(page, offset, sort).either(::onFailure, ::onSuccess)
        }

    private fun onSuccess(it: ArrayList<Episode>) {
        progress.postValue(false)
        episodes.postValue(it)
    }
}