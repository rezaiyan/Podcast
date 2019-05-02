package com.hezaro.wall.data.model

data class Login(
    var meta: Meta,
    var response: UserInfo
)

data class UserInfo(
    val username: String = "",
    val avatar: String = "",
    var email: String = "",
    var jwt: String = ""
)