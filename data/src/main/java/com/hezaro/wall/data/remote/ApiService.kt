package com.hezaro.wall.data.remote

import com.hezaro.wall.data.model.Explore
import retrofit2.Call
import retrofit2.http.GET

private const val BASE = "api/v1"
interface ApiService {

    @GET("$BASE/episodes")
    fun explore(): Call<Explore>
}
