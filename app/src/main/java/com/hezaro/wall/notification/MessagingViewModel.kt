package com.hezaro.wall.notification

import com.hezaro.wall.domain.MessagingRepository
import com.hezaro.wall.sdk.platform.BaseViewModel
import kotlinx.coroutines.launch

class MessagingViewModel(private val repository: MessagingRepository) : BaseViewModel() {

    fun sendToken(token: String) = launch { repository.sendToken(token) }
}