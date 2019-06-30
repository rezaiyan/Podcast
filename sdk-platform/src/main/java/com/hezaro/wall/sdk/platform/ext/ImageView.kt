package com.hezaro.wall.sdk.platform.ext

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.webkit.URLUtil
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.RequestCreator
import com.squareup.picasso.Target
import com.squareup.picasso.Transformation

fun ImageView.load(
    imageUrl: String, transformation: Transformation = DefaultTransformation()
) {
    if (URLUtil.isValidUrl(imageUrl))
        Picasso.get().load(imageUrl).transform(transformation).into(this)
}

fun ImageView.loadBlur(
    imageUrl: String, @DrawableRes drawable: Int = 0,
    transformation: Transformation = DefaultTransformation()
) {

    if (URLUtil.isValidUrl(imageUrl))
        Picasso.get().loadWith(imageUrl, drawable).transform(transformation).into(object : Target {
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable) {
            }

            override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom?) {
//                setBackgroundDrawable(BitmapDrawable(resources, BlurImage.fastblur(bitmap, 0.4f, 9)))
//                setImageBitmap(BlurImage.blur(context,bitmap))
                background = BitmapDrawable(resources, bitmap)
            }
        })
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