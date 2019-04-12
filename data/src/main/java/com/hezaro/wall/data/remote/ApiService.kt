package com.hezaro.wall.data.remote

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {

    @GET("/")
    fun explore(): Call<Nothing>
}
