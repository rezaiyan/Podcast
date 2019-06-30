package com.hezaro.wall.sdk.platform.ext

import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE

fun View.show() {
    visibility = VISIBLE
}

fun View.gone() {
    visibility = GONE
}

fun View.hide() {
    visibility = INVISIBLE
}