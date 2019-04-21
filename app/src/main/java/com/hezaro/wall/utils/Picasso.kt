package com.hezaro.wall.utils

import android.webkit.URLUtil
import androidx.annotation.DrawableRes
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator

fun Picasso.load(cover: String, @DrawableRes drawable: Int): RequestCreator {
    var standardizedCover = cover
    if (!cover.startsWith("http")) {
        standardizedCover = "http://$cover"
    }

    return if (URLUtil.isValidUrl(standardizedCover)) {
        this.load(standardizedCover)
    } else this.load(drawable)
}