package com.hezaro.wall.sdk.platform.ext

import android.graphics.Bitmap
import android.webkit.URLUtil
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.hezaro.wall.sdk.platform.R
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import com.squareup.picasso.Transformation

fun ImageView.load(
    imageUrl: String, transformation: Transformation = DefaultTransformation()
) {
    if (URLUtil.isValidUrl(imageUrl))
        Picasso.get()
            .load(imageUrl)
            .placeholder(R.drawable.ic_ph)
            .error(R.drawable.ic_ph)
            .transform(transformation).into(this)
}

fun Picasso.loadWith(cover: String, @DrawableRes drawable: Int): RequestCreator {
    var standardizedCover = cover
    if (!cover.startsWith("http")) {
        standardizedCover = "http://$cover"
    }

    return if (URLUtil.isValidUrl(standardizedCover)) {
        this.load(standardizedCover)
    } else this.load(drawable)
}

class DefaultTransformation : Transformation {
    override fun key() = ""
    override fun transform(source: Bitmap?): Bitmap = source!!
}