package com.hezaro.wall.domain

import android.content.SharedPreferences
import com.hezaro.wall.data.base.BaseRepository
import com.hezaro.wall.data.remote.ApiService
import com.hezaro.wall.sdk.base.extention.EMAIL
import com.hezaro.wall.sdk.base.extention.get

interface EpisodeRepository {

    fun userIsLogin(): Boolean
    fun bookmarkAction(bookmark: Boolean, id: Long)

    class EpisodeRepositoryImpl(
        private val api: ApiService,
        private val storage: SharedPreferences
    ) :
        BaseRepository(),
        EpisodeRepository {

        override fun bookmarkAction(bookmark: Boolean, id: Long) {
            if (bookmark)
                request(api.bookmark(id)) {}
            else
                request(api.unBookmark(id)) {}
        }

        override fun userIsLogin() = storage.get(EMAIL, "").isNotEmpty()

    }
}