package com.hezaro.wall.feature.core.main

import com.hezaro.wall.data.model.UserInfo
import com.hezaro.wall.domain.LoginRepository
import com.hezaro.wall.sdk.platform.BaseViewModel
import kotlinx.coroutines.launch

class MainViewModel(private val repository: LoginRepository) : BaseViewModel<UserInfo>() {

    fun login(idToken: String) = launch(job) {
        repository.login(idToken).either(::onFailure, ::onSuccess)
    }
}