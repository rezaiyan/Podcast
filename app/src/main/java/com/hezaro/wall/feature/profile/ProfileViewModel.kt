package com.hezaro.wall.feature.profile

import com.hezaro.wall.data.model.UserInfo
import com.hezaro.wall.domain.ProfileRepository
import com.hezaro.wall.sdk.platform.BaseViewModel
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: ProfileRepository) : BaseViewModel<UserInfo>() {
    fun userInfo() = launch(job) { repository.userInfo().either(::onFailure, ::onSuccess) }
}