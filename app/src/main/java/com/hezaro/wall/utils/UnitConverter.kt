package com.hezaro.wall.utils

import android.content.res.Resources

/**
 * @author ali (alirezaiyann@gmail.com)
 * @since 8/6/19 6:18 PM.
 */

fun Number.toDp() = (this.toInt() / Resources.getSystem().displayMetrics.density).toInt()

fun Number.toPx() = (this.toInt() * Resources.getSystem().displayMetrics.density).toInt()