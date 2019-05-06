package com.hezaro.wall.feature.episode

import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.domain.EpisodeRepository
import com.hezaro.wall.sdk.platform.BaseViewModel
import kotlinx.coroutines.launch

class EpisodeViewModel(private val repository: EpisodeRepository) : BaseViewModel() {

    fun save(episode: Episode) = launch { repository.save(episode) }
    fun delete(episode: Episode) = launch { repository.delete(episode) }
}