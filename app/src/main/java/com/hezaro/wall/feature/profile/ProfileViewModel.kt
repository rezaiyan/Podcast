package com.hezaro.wall.feature.profile

import androidx.lifecycle.MutableLiveData
import com.hezaro.wall.data.model.UserInfo
import com.hezaro.wall.domain.ProfileRepository
import com.hezaro.wall.sdk.platform.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: ProfileRepository) : BaseViewModel() {

    val userInfo: MutableLiveData<UserInfo> = MutableLiveData()

    fun userInfo() = launch(job) {
        isExecute = true
        repository.userInfo().either(::onFailure, ::onSuccess)
    }

    private fun onSuccess(it: UserInfo) = launch(Dispatchers.Main) {
        isExecute = false
        userInfo.value = it
    }
}