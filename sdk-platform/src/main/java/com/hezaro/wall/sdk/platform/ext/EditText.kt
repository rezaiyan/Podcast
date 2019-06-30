package com.hezaro.wall.sdk.platform.ext

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.hezaro.wall.sdk.platform.R

fun SearchView.normalize(font: Int) {
    val editText = findViewById<View>(R.id.search_src_text) as EditText
    editText.gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
    editText.hint = "جستجو"
    editText.typeface = ResourcesCompat.getFont(context, font)
    editText.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
    editText.setTextColor(ContextCompat.getColor(context, R.color.black))
    editText.setHintTextColor(ContextCompat.getColor(context, R.color.colorTextSecondary))
    editText.setCursorDrawableColor(ContextCompat.getColor(context, R.color.colorTextSecondary))
}

fun EditText.setCursorDrawableColor(color: Int) {
    try {
        val fCursorDrawableRes = TextView::class.java.getDeclaredField("mCursorDrawableRes")
        fCursorDrawableRes.isAccessible = true
        val mCursorDrawableRes = fCursorDrawableRes.getInt(this)
        val fEditor = TextView::class.java.getDeclaredField("mEditor")
        fEditor.isAccessible = true
        val editor = fEditor.get(this)
        val clazz = editor.javaClass
        val fCursorDrawable = clazz.getDeclaredField("mCursorDrawable")
        fCursorDrawable.isAccessible = true

        val drawables = arrayOfNulls<Drawable>(2)
        drawables[0] = ContextCompat.getDrawable(context, mCursorDrawableRes)
        drawables[1] = ContextCompat.getDrawable(context, mCursorDrawableRes)
        drawables[0]?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        drawables[1]?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        fCursorDrawable.set(editor, drawables)
    } catch (ignored: Throwable) {
    }
}
