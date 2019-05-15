package com.hezaro.wall.feature.player

import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.domain.PlayerRepository
import com.hezaro.wall.sdk.platform.BaseViewModel
import kotlinx.coroutines.launch

class PlayerViewModel(private val repository: PlayerRepository) : BaseViewModel() {

    fun speed(speed: Float) =
        launch(job) { repository.setSpeed(speed) }

    fun defaultSpeed() = repository.getSpeed()

    fun sendLastPosition(id: Long, lastState: Long) =
        launch(job) {
            repository.sendLastPosition(id, lastState)
        }

    fun saveLatestEpisode(episode: Episode) =
        launch(job) { repository.savePlayedEpisode(episode) }

    fun sendLikeAction(like: Boolean, id: Long) =
        launch(job) {
            repository.likeAction(like, id)
        }

    fun sendBookmarkAction(like: Boolean, id: Long) =
        launch(job) {
            repository.bookmarkAction(like, id)
        }

    fun userIsLogin() = repository.userIsLogin()

    fun updateEpisode(it: Episode) = launch(job) {
        repository.updateEpisode(it)
    }
}