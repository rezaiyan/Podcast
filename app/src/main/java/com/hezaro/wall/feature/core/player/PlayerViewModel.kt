package com.hezaro.wall.feature.core.player

import com.hezaro.wall.data.model.UserInfo
import com.hezaro.wall.domain.PlayerRepository
import com.hezaro.wall.sdk.platform.BaseViewModel
import kotlinx.coroutines.launch

class PlayerViewModel(private val repository: PlayerRepository) : BaseViewModel<UserInfo>() {

    fun speed(speed: Float) = launch(job) { repository.setSpeed(speed) }
    fun defaultSpeed() = repository.getSpeed()
}