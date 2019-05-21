package com.hezaro.wall.domain

import android.content.SharedPreferences
import com.hezaro.wall.data.base.BaseRepository
import com.hezaro.wall.data.local.EpisodeDao
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.UserInfo
import com.hezaro.wall.data.remote.ApiService
import com.hezaro.wall.sdk.base.Either
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.base.extention.EMAIL
import com.hezaro.wall.sdk.base.extention.THEME
import com.hezaro.wall.sdk.base.extention.USER_NAME
import com.hezaro.wall.sdk.base.extention.get
import com.hezaro.wall.sdk.base.extention.put
import io.reactivex.Flowable

interface ProfileRepository {

    fun userInfo(): Either<Failure, UserInfo>
    fun getDownloads(): Flowable<ArrayList<Episode>>
    fun getBookmarks(): Either<Failure, ArrayList<Episode>>
    fun setThemeStatus(night: Boolean)

    class ProfileRepositoryImpl(
        private val api: ApiService,
        private val storage: SharedPreferences,
        private val database: EpisodeDao
    ) :
        BaseRepository(),
        ProfileRepository {

        override fun setThemeStatus(night: Boolean) = storage.put(THEME, night)
        override fun getBookmarks(): Either<Failure, ArrayList<Episode>> =
            request(api.bookmarks()) { remote ->
                val localList = database.getAllEpisodes()
                remote.response.forEach { e ->
                    if (localList.contains(e))
                        localList[localList.indexOf(e)].update(e)
                }

                remote.response

            }

        override fun getDownloads() = database.getDownloadEpisodes().map { ArrayList(it) }!!
        override fun userInfo(): Either<Failure, UserInfo> {
            with(storage) {
                val username = get(USER_NAME, "")
                val email = get(EMAIL, "")
                return if (email.isNotEmpty() && username.isNotEmpty())
                    Either.Right(UserInfo(username, email))
                else Either.Left(Failure.UserNotFound())
            }
        }
    }
}