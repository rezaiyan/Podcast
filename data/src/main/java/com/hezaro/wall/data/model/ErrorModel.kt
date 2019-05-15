package com.hezaro.wall.data.model

import androidx.annotation.Keep

@Keep
class ErrorModel(val status: Boolean, val msg: String, val success: Boolean)
