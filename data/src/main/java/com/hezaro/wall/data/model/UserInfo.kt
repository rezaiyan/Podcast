package com.hezaro.wall.data.model

import androidx.annotation.Keep

@Keep
data class UserInfo(
    val username: String = "",
    val avatar: String = "",
    var email: String = "",
    var jwt: String = ""
)