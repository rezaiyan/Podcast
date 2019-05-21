package com.hezaro.wall.data.model

import androidx.annotation.IntDef
import androidx.annotation.Keep
import androidx.annotation.StringDef
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

const val DOWNLOADED = 1
const val IS_NOT_DOWNLOADED = 0

@Keep
class Status{

companion object {

    @Target(AnnotationTarget.TYPE, AnnotationTarget.PROPERTY)
    @IntDef(DOWNLOADED, IS_NOT_DOWNLOADED)
    @Retention(RetentionPolicy.SOURCE)
    annotation class DownloadStatus

    const val NEW = 1
    const val IN_PROGRESS = 2
    const val PLAYED = 3


    const val BEST = "best"
    const val BEST_ = "بهترین ها"
    const val OLDEST = "old"
    const val OLDEST_ = "قدیمی ترین"
    const val NEWEST = "new"
    const val NEWEST_ = "جدیدترین"

    @Target(AnnotationTarget.TYPE, AnnotationTarget.PROPERTY)
    @StringDef(BEST, OLDEST, NEWEST)
    @Retention(RetentionPolicy.SOURCE)
    annotation class SortBy

}
}
