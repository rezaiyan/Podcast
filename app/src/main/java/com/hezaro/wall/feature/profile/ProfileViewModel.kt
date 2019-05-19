package com.hezaro.wall.feature.profile

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.UserInfo
import com.hezaro.wall.domain.ProfileRepository
import com.hezaro.wall.sdk.platform.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: ProfileRepository) : BaseViewModel() {

    val userInfo: MutableLiveData<UserInfo> = MutableLiveData()
    val episodes: MutableLiveData<ArrayList<Episode>> = MutableLiveData()

    fun userInfo() =
        launch {
            isExecute = true
            repository.userInfo().either(::onFailure, ::onSuccess)
        }

    private fun onSuccess(it: UserInfo) {
        isExecute = false
        userInfo.postValue(it)
    }

    @SuppressLint("CheckResult")
    fun getEpisodes() {
        repository.getDownloadEpisodes()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                episodes.value = it
            }
    }

    fun setThemeStatus(night: Boolean) = repository.setThemeStatus(night)
}