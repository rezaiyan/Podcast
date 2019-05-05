package com.hezaro.wall.feature.core.player

import com.hezaro.wall.domain.PlayerRepository
import com.hezaro.wall.sdk.platform.BaseViewModel
import kotlinx.coroutines.launch

class PlayerViewModel(private val repository: PlayerRepository) : BaseViewModel() {

    fun speed(speed: Float) = launch(job) { repository.setSpeed(speed) }
    fun defaultSpeed() = repository.getSpeed()
    fun savePosition(id: Int, lastState: Long) = launch(job) {
        repository.sendLastPosition(id, lastState)
    }
}