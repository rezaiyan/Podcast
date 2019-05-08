package com.hezaro.wall.feature.core.player

import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.domain.PlayerRepository
import com.hezaro.wall.sdk.platform.BaseViewModel
import kotlinx.coroutines.launch

class PlayerViewModel(private val repository: PlayerRepository) : BaseViewModel() {

    fun speed(speed: Float) = launch(job) { repository.setSpeed(speed) }
    fun defaultSpeed() = repository.getSpeed()
    fun sendLastPosition(id: Long, lastState: Long) = launch(job) {
        repository.sendLastPosition(id, lastState)
    }
    fun saveLatestEpisode(episode: Episode)  = launch { repository.savePlayedEpisode(episode) }
    fun retrieveLatestEpisode()  = launch { repository.retrieveLatestPlayedEpisode() }
    fun sendLikeAction(like: Boolean, id: Long) = launch {
        repository.sendLikeAction(like, id)
    }
}