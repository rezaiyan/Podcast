package com.hezaro.wall.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Response<T>(
    @SerializedName("meta")
    var meta: Meta,
    @SerializedName("response")
    var response: T
)

data class Meta(
    @SerializedName("status")
    val status: Int,
    @SerializedName("message")
    val message: String
)