package com.hezaro.wall.feature.podcast

import androidx.lifecycle.MutableLiveData
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.domain.PodcastRepository
import com.hezaro.wall.sdk.platform.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PodcastViewModel(private val repository: PodcastRepository) : BaseViewModel() {

    val episodes = MutableLiveData<ArrayList<Episode>>()

    fun getEpisodes(podcastId: Int) =
        launch(job) {
            isExecute = true
            repository.getEpisodes(podcastId).either(::onFailure, ::onSuccess)
        }

    private fun onSuccess(it: ArrayList<Episode>) =
        launch(Dispatchers.Main) {
            isExecute = false
            episodes.value = it
        }
}