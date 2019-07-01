package com.hezaro.wall.data.remote

import com.hezaro.wall.data.BuildConfig
import com.hezaro.wall.data.model.DExplore
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Podcast
import com.hezaro.wall.data.model.Response
import com.hezaro.wall.data.model.Status.Companion.SortBy
import com.hezaro.wall.data.model.UserInfo
import com.hezaro.wall.data.model.Version
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

private const val BASE = "api/v1"

interface ApiService {

    @GET("$BASE/explore")
    fun explore(): Call<DExplore>

    @GET("$BASE/episodes")
    fun episodes(@Query("sort_by") sort: @SortBy String, @Query("page") page: Int = 1, @Query("offset") offset: Int = 20): Call<Response<ArrayList<Episode>>>

    @GET("$BASE/podcasts")
    fun podcasts(@Query("page") page: Int = 1, @Query("offset") offset: Int = 20): Call<Response<ArrayList<Podcast>>>

    @GET("$BASE/podcasts/{id}")
    fun podcast(@Path("id") id: Long): Call<Response<Podcast>>

    @GET("$BASE/podcasts/{episode_id}/episodes")
    fun episodesOfPodcast(@Path("episode_id") episode_id: Long, @Query("page") page: Int = 1, @Query("offset") offset: Int = 20): Call<Response<ArrayList<Episode>>>

    @GET("$BASE/episodes/{id}")
    fun episode(@Path("id") id: Long): Call<Response<Episode>>

    @GET("$BASE/bookmarks")
    fun bookmarks(): Call<Response<ArrayList<Episode>>>

    @FormUrlEncoded
    @POST("$BASE/login")
    fun login(@Field("id_token") id_token: String): Call<Response<UserInfo>>

    @GET("$BASE/version")
    fun version(@Query("version_code") version_code: Int = BuildConfig.VERSION_CODE): Call<Response<Version>>

    @FormUrlEncoded
    @POST("$BASE/episodes/{episode_id}/state")
    fun sendLastPosition(@Path("episode_id") episode_id: Long, @Field("state") state: Long): Call<Response<Any>>

    @DELETE("$BASE/likes/{episode_id}")
    fun disLike(@Path("episode_id") episode_id: Long): Call<Response<Any>>

    @POST("$BASE/likes/{episode_id}")
    fun like(@Path("episode_id") episode_id: Long): Call<Response<Any>>

    @DELETE("$BASE/bookmarks/{episode_id}")
    fun unBookmark(@Path("episode_id") episode_id: Long): Call<Response<Any>>

    @POST("$BASE/bookmarks/{episode_id}")
    fun bookmark(@Path("episode_id") episode_id: Long): Call<Response<Any>>

    @FormUrlEncoded
    @POST("$BASE/user/notification_token")
    fun sendApi(@Field("token") token: String): Call<Response<Any>>

    @GET("$BASE/search")
    fun search(@Query("q") query: String, @Query("page") page: Int = 1, @Query("offset") offset: Int = 20): Call<Response<ArrayList<Episode>>>
}
