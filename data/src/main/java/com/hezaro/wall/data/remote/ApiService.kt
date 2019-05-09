package com.hezaro.wall.data.remote

import com.hezaro.wall.data.BuildConfig
import com.hezaro.wall.data.model.Episode
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

    @GET("$BASE/episodes")
    fun explore(@Query("sort_by") sort: @SortBy String, @Query("page") page: Int = 1, @Query("offset") offset: Int = 20): Call<Response<MutableList<Episode>>>

    @FormUrlEncoded
    @POST("$BASE/login")
    fun login(@Field("id_token") id_token: String): Call<Response<UserInfo>>

    @FormUrlEncoded
    @POST("$BASE/version")
    fun version(@Field("version_code") version_code: Int = BuildConfig.VERSION_CODE): Call<Response<Version>>

    @FormUrlEncoded
    @POST("$BASE/episodes/{episode_id}/state")
    fun sendLastPosition(@Path("episode_id") episode_id: Long, @Field("state") state: Long): Call<Response<Any>>

    @DELETE("$BASE/likes/{episode_id}")
    fun disLike(@Path("episode_id") episode_id: Long): Call<Response<Any>>

    @FormUrlEncoded
    @POST("$BASE/likes/{episode_id}")
    fun like(@Path("episode_id") episode_id: Long, @Field("state") state: Long = 0): Call<Response<Any>>

    @FormUrlEncoded
    @POST("$BASE/login")
    fun sendApi(@Field("token") token: String): Call<Response<Any>>

    @GET("$BASE/search")
    fun search(@Query("q") query: String, @Query("page") page: Int = 1, @Query("offset") offset: Int = 20): Call<Response<MutableList<Episode>>>
}
