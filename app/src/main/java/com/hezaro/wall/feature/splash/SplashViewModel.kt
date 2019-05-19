package com.hezaro.wall.feature.splash

import androidx.lifecycle.MutableLiveData
import com.hezaro.wall.data.model.Version
import com.hezaro.wall.domain.SplashRepository
import com.hezaro.wall.sdk.platform.BaseViewModel
import kotlinx.coroutines.launch

class SplashViewModel(private val repository: SplashRepository) : BaseViewModel() {

    var version: MutableLiveData<Version> = MutableLiveData()

    fun version() =
        launch {
            isExecute = true
            repository.version().either(::onFailure, ::onVersion)
        }

    private fun onVersion(it: Version) {
        isExecute = false
        version.postValue(it)
    }

    fun isNight() = repository.isNight()
}