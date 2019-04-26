package com.hezaro.wall.data.remote

import com.hezaro.wall.data.model.Explore
import com.hezaro.wall.data.model.Login
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

private const val BASE = "api/v1"

interface ApiService {

    @GET("$BASE/episodes")
    fun explore(@Query("sort_by") sort: String = "best", @Query("page") page: Int = 1, @Query("offset") offset: Int = 20): Call<Explore>

    @POST("$BASE/login")
    fun login(@Body id_token: String): Call<Login>
}
