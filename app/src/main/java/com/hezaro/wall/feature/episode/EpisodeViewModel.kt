package com.hezaro.wall.feature.episode

import com.hezaro.wall.domain.EpisodeRepository
import com.hezaro.wall.sdk.platform.BaseViewModel
import kotlinx.coroutines.launch

class EpisodeViewModel(private val repository: EpisodeRepository) : BaseViewModel() {

    fun userIsLogin() = repository.userIsLogin()

    fun sendBookmarkAction(like: Boolean, id: Long) =
        launch {
            repository.bookmarkAction(like, id)
        }

}