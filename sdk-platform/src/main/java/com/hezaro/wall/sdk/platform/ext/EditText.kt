package com.hezaro.wall.sdk.platform.ext

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import com.hezaro.wall.sdk.platform.R
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import java.util.concurrent.TimeUnit

fun SearchView.normalize() {
    val editText = findViewById<View>(R.id.search_src_text) as EditText
    editText.gravity = Gravity.RIGHT
    editText.hint = "جستجو"
    editText.setBackgroundColor(resources.getColor(android.R.color.transparent))
    editText.setTextColor(resources.getColor(R.color.colorTextPrimary))
    editText.setHintTextColor(resources.getColor(R.color.colorTextSecondary))
    editText.setCursorDrawableColor(resources.getColor(R.color.colorTextSecondary))
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
        val res = context.resources
        drawables[0] = res.getDrawable(mCursorDrawableRes)
        drawables[1] = res.getDrawable(mCursorDrawableRes)
        drawables[0]?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        drawables[1]?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        fCursorDrawable.set(editor, drawables)
    } catch (ignored: Throwable) {
    }
}

@SuppressLint("CheckResult")
infix fun SearchView.search(function: (String) -> Unit) {

    Observable.create(ObservableOnSubscribe<String> { subscriber ->
        setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                subscriber.onNext(newText!!)
                return false
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                subscriber.onNext(query!!)
                return false
            }
        })
    })
        .map { text -> text.toLowerCase().trim() }
        .debounce(250, TimeUnit.MILLISECONDS)
        .distinct()
        .filter { text -> text.length > 2 }
        .subscribe { text -> function(text) }
}