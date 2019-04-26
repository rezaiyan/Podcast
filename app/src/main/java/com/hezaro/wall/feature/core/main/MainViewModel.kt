package com.hezaro.wall.feature.core.main

import androidx.lifecycle.MutableLiveData
import com.hezaro.wall.data.model.UserInfo
import com.hezaro.wall.domain.LoginRepository
import com.hezaro.wall.sdk.platform.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(private val repository: LoginRepository) : BaseViewModel() {

    val userInfo: MutableLiveData<UserInfo> = MutableLiveData()

    fun login(idToken: String) {
        launch(job) {
            repository.login(idToken).either(::onFailure, ::onLogin)
        }
    }

    private fun onLogin(it: UserInfo) {
        launch(Dispatchers.Main) { userInfo.value = it }
    }
}