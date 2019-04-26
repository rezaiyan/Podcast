package com.hezaro.wall.feature.profile

import androidx.lifecycle.MutableLiveData
import com.hezaro.wall.data.model.UserInfo
import com.hezaro.wall.domain.ProfileRepository
import com.hezaro.wall.sdk.platform.BaseViewModel

class ProfileViewModel(private val repository: ProfileRepository) : BaseViewModel() {

    val userInfo: MutableLiveData<UserInfo> = MutableLiveData()

    fun userInfo() {
        repository.userInfo().either(::onFailure, ::onLoadData)
    }

    private fun onLoadData(userInfo: UserInfo) {
        this.userInfo.value = userInfo
    }
}