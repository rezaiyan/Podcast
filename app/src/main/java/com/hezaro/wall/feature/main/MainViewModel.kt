package com.hezaro.wall.feature.main

import androidx.lifecycle.MutableLiveData
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Podcast
import com.hezaro.wall.data.model.UserInfo
import com.hezaro.wall.domain.MainRepository
import com.hezaro.wall.domain.PlayerRepository
import com.hezaro.wall.sdk.platform.BaseViewModel
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: MainRepository,
    private val playerRepository: PlayerRepository
) :
    BaseViewModel() {

    var login: MutableLiveData<UserInfo> = MutableLiveData()
    var episode: MutableLiveData<Episode> = MutableLiveData()
    var deepEpisode: MutableLiveData<Episode> = MutableLiveData()
    var deepPodcast: MutableLiveData<Podcast> = MutableLiveData()

    fun login(idToken: String) =
        launch {
            progress.postValue(true)
            repository.login(idToken).either(::onFailure, ::onLoad)
        }

    fun defaultSpeed() = playerRepository.getSpeed()

    private fun onLoad(it: UserInfo) {
        progress.postValue(false)
        login.postValue(it)
    }

    fun retrieveLatestEpisode() =
        launch {
            val it = repository.retrieveLatestPlayedEpisode()
            it?.let { episode.postValue(it) }

        }

    fun userInfo() = repository.getUserInfo()

    fun getEpisode(id: Long) {
        launch {
            progress.postValue(true)
            repository.getEpisode(id).either(::onFailure, ::onLoad)
        }
    }

    fun getPodcast(id: Long) {
        launch {
            progress.postValue(true)
//            repository.getPodcast(id).either(::onFailure, ::onLoad)
        }
    }

    private fun onLoad(podcast: Podcast) {
        progress.postValue(false)
        deepPodcast.postValue(podcast)
    }

    private fun onLoad(episode: Episode) {
        progress.postValue(false)
        deepEpisode.postValue(episode)
    }
}