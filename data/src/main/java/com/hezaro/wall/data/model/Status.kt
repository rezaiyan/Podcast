package com.hezaro.wall.data.model

import androidx.annotation.IntDef
import androidx.annotation.StringDef
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy



class Status{

companion object {

    const val DOWNLOADED: Int = 3
    const val DOWNLOADING: Int = 2
    const val NOT_DOWNLOADED: Int = 1

    @Target(AnnotationTarget.TYPE, AnnotationTarget.PROPERTY)
    @IntDef(DOWNLOADED, DOWNLOADING, NOT_DOWNLOADED)
    @Retention(RetentionPolicy.SOURCE)
    annotation class DownloadStatus

    const val NEW = 1
    const val IN_PROGRESS = 2
    const val PLAYED = 3

    @Target(AnnotationTarget.TYPE, AnnotationTarget.PROPERTY)
    @IntDef(DOWNLOADED, DOWNLOADING, NOT_DOWNLOADED)
    @Retention(RetentionPolicy.SOURCE)
    annotation class PlayStatus

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
