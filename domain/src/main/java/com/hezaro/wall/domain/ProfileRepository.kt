package com.hezaro.wall.domain

import android.content.SharedPreferences
import com.hezaro.wall.data.local.EpisodeDao
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.UserInfo
import com.hezaro.wall.data.utils.BaseRepository
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
    fun getDownloadEpisodes(): Flowable<ArrayList<Episode>>
    fun setThemeStatus(night: Boolean)

    class ProfileRepositoryImpl(private val storage: SharedPreferences, private val database: EpisodeDao) :
        BaseRepository(),
        ProfileRepository {

        override fun setThemeStatus(night: Boolean) = storage.put(THEME, night)
        override fun getDownloadEpisodes() = database.getDownloadEpisodes().map { ArrayList(it) }!!
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