package com.hezaro.wall.feature.profile

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.domain.ProfileRepository
import com.hezaro.wall.sdk.platform.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: ProfileRepository) : BaseViewModel() {

    val downloadEpisodes: MutableLiveData<ArrayList<Episode>> = MutableLiveData()
    val bookmarkEpisodes: MutableLiveData<ArrayList<Episode>> = MutableLiveData()

    fun getBookmarks() =
        launch {
            progress.postValue(true)
            repository.getBookmarks().either(::onFailure, ::onSuccessBookmark)
        }

    @SuppressLint("CheckResult")
    fun getDownloads() {
        repository.getDownloads()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                downloadEpisodes.value = it
            }
    }

    private fun onSuccessBookmark(it: ArrayList<Episode>) {
        progress.postValue(false)
        bookmarkEpisodes.postValue(it)
    }

    fun setThemeStatus(night: Boolean) = repository.setThemeStatus(night)
}