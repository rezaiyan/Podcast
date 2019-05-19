package com.hezaro.wall.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class UserInfo(
    @SerializedName("username")
    val username: String = "",
    @SerializedName("avatar")
    val avatar: String = "",
    @SerializedName("email")
    var email: String = "",
    @SerializedName("jwt")
    var jwt: String = ""
)