package com.hezaro.wall.feature.explore

import androidx.lifecycle.MutableLiveData
import com.hezaro.wall.data.model.Explore
import com.hezaro.wall.domain.ExploreRepository
import com.hezaro.wall.sdk.platform.BaseViewModel
import kotlinx.coroutines.launch

/**
 * @author ali (alirezaiyann@gmail.com)
 * @since 6/25/19 10:45 AM.
 */

class ExploreViewModel(private val repository: ExploreRepository) : BaseViewModel() {

    val explore: MutableLiveData<Explore> = MutableLiveData()

    fun explore() {
        launch {
            repository.explore().either(::onFailure, ::onSuccess)
        }
    }

    private fun onSuccess(it: Explore) = explore.postValue(it)
}