package com.hezaro.wall.sdk.platform.ext

/**
 * @author ali (alirezaiyann@gmail.com)
 * @since 6/18/19 4:38 PM.
 */

fun Number.toRange(old: IntRange, new: IntRange) =
    ((((this.toFloat() - old.first) * (new.last - new.first)) / (old.last - old.first)) + new.first)
