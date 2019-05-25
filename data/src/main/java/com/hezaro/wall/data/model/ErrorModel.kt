package com.hezaro.wall.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

data class ErrorBody(@SerializedName("meta") val meta: ErrorModel)
@Keep
class ErrorModel(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String
)
