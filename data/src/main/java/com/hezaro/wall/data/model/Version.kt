package com.hezaro.wall.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Version(
    @SerializedName("version")
    val version: Long,
    @SerializedName("force_update")
    val force_update: Boolean
)