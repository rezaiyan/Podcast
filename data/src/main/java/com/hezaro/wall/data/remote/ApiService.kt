package com.hezaro.wall.data.remote

import com.hezaro.wall.data.BuildConfig
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Response
import com.hezaro.wall.data.model.UserInfo
import com.hezaro.wall.data.model.Version
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

private const val BASE = "api/v1"

interface ApiService {

    @GET("$BASE/episodes")
    fun explore(@Query("sort_by") sort: String = "best", @Query("page") page: Int = 1, @Query("offset") offset: Int = 20): Call<Response<MutableList<Episode>>>

    @POST("$BASE/login")
    fun login(@Body id_token: String): Call<Response<UserInfo>>

    @POST("$BASE/version")
    fun version(@Body version_code: Int = BuildConfig.VERSION_CODE): Call<Response<Version>>
}
