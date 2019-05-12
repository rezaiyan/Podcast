package com.hezaro.wall.feature.splash

import androidx.lifecycle.MutableLiveData
import com.hezaro.wall.data.model.Version
import com.hezaro.wall.domain.SplashRepository
import com.hezaro.wall.sdk.platform.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SplashViewModel(private val repository: SplashRepository) : BaseViewModel() {

    var version: MutableLiveData<Version> = MutableLiveData()

    fun version() =
        launch(job) {
            isExecute = true
            repository.version().either(::onFailure, ::onVersion)
        }

    private fun onVersion(it: Version) =
        launch(Dispatchers.Main) {
            isExecute = false
            version.value = it
        }
}