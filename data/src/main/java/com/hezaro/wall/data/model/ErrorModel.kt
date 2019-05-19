package com.hezaro.wall.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
class ErrorModel(
    @SerializedName("status") val status: Boolean,
    @SerializedName("msg") val msg: String
)
