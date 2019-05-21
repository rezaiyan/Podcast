package com.hezaro.wall.feature.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.UserInfo

class SharedViewModel : ViewModel() {

    val listMargin = MutableLiveData<Int>()
    val progressMargin = MutableLiveData<Int>()
    val resetPlaylist = MutableLiveData<Boolean>()
    val userInfo = MutableLiveData<UserInfo>()
    val episode = MutableLiveData<Pair<Int, Episode>>()
    val playStatus = MutableLiveData<Int>()
    val sheetState = MutableLiveData<Int>()
    val collapseSheet = MutableLiveData<Int>()
    val playerIsOpen = MutableLiveData<Boolean>()
    val isServiceConnected = MutableLiveData<Boolean>()
    val isPlaying = MutableLiveData<Boolean>()

    fun listMargin(i: Int) {
        listMargin.value = i
    }

    fun progressMargin(i: Int) {
        progressMargin.value = i
    }

    fun resetPlaylist(it: Boolean?) {
        resetPlaylist.value = it
    }


    fun notifyEpisode(it: Pair<Int, Episode>) {
        episode.value = it
    }

    fun notifyPlayStatus(it: Int) {
        playStatus.value = it
    }

    fun updateSheetState(it: Int) {
        sheetState.value = it
    }

    fun isPlayerExpand() = sheetState.value == BottomSheetBehavior.STATE_EXPANDED

    fun collapsePlayer() {
        collapseSheet.value = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun playerIsOpen(b: Boolean) {
        playerIsOpen.value = b
    }

    fun serviceConnection(b: Boolean) {
        isServiceConnected.value = b
    }

    fun isPlaying(b: Boolean) {
        isPlaying.value = b
    }
    fun userInfo(it: UserInfo?) {
        userInfo.value = it
    }
}