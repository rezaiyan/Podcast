package com.hezaro.wall.data.model

data class Response<T>(
    var meta: Meta,
    var response: T
)

data class Meta(
    val status: Int,
    val message: String
)