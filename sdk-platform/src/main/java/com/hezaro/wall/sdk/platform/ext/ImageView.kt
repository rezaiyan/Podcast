package com.hezaro.wall.sdk.platform.ext

import android.graphics.Bitmap
import android.widget.ImageView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation

fun ImageView.load(imageUrl: String?, transformation: Transformation = DefaultTransformation()) {

    Picasso.get().load(imageUrl).transform(transformation).into(this)
}

class DefaultTransformation : Transformation {
    override fun key() = ""
    override fun transform(source: Bitmap?): Bitmap = source!!
}