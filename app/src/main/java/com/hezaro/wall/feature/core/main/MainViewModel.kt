package com.hezaro.wall.feature.core.main

import androidx.lifecycle.MutableLiveData
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.UserInfo
import com.hezaro.wall.data.model.Version
import com.hezaro.wall.domain.MainRepository
import com.hezaro.wall.domain.PlayerRepository
import com.hezaro.wall.sdk.platform.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: MainRepository? = null,
    private val playerRepository: PlayerRepository? = null
) :
    BaseViewModel() {

    var login: MutableLiveData<UserInfo> = MutableLiveData()
    var version: MutableLiveData<Version> = MutableLiveData()
    var episode: MutableLiveData<Episode> = MutableLiveData()

    fun login(idToken: String) = launch(job) {
        isExecute = true
        repository!!.login(idToken).either(::onFailure, ::onLogin)
    }

    fun version() = launch(job) {
        isExecute = true
        repository?.version()?.either(::onFailure, ::onVersion)
    }

    fun defaultSpeed() = playerRepository!!.getSpeed()

    private fun onVersion(it: Version) {
        isExecute = false
        launch(Dispatchers.Main) { version.value = it }
    }

    private fun onLogin(it: UserInfo) {
        isExecute = false
        launch(Dispatchers.Main) { login.value = it }
    }

    fun retrieveLatestEpisode() = launch {
        val it = repository!!.retrieveLatestPlayedEpisode()
        launch(Dispatchers.Main) { it?.let { episode.value = it } }
    }
}