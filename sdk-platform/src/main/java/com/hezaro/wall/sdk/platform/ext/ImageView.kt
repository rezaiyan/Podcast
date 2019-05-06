package com.hezaro.wall.sdk.platform.ext

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.webkit.URLUtil
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.hezaro.wall.sdk.platform.utils.BlurImage
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.RequestCreator
import com.squareup.picasso.Target
import com.squareup.picasso.Transformation

fun ImageView.load(
    imageUrl: String, @DrawableRes drawable: Int = android.R.drawable.radiobutton_on_background,
    transformation: Transformation = DefaultTransformation()
) {

    Picasso.get().loadWith(imageUrl, drawable).transform(transformation).into(this)
}

fun ImageView.loadBlur(
    imageUrl: String, @DrawableRes drawable: Int = android.R.drawable.radiobutton_on_background,
    transformation: Transformation = DefaultTransformation()
) {

    Picasso.get().loadWith(imageUrl, drawable).transform(transformation).into(object : Target {
        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
        }

        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable) {
        }

        override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom?) {
            background = BitmapDrawable(resources, BlurImage.blur(context, bitmap))
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