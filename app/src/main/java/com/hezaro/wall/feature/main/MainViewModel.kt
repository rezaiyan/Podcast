package com.hezaro.wall.feature.main

import androidx.lifecycle.MutableLiveData
import com.hezaro.wall.data.model.Episode
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

    fun login(idToken: String) =
        launch {
            isExecute = true
            repository.login(idToken).either(::onFailure, ::onLogin)
        }

    fun defaultSpeed() = playerRepository.getSpeed()

    private fun onLogin(it: UserInfo) {
        isExecute = false
        login.postValue(it)
    }

    fun retrieveLatestEpisode() =
        launch {
            val it = repository.retrieveLatestPlayedEpisode()
            it?.let { episode.postValue(it) }

        }

    fun userInfo() = repository.getUserInfo()
    fun signOut() = repository.signOut()
}