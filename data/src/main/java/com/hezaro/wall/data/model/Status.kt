package com.hezaro.wall.data.model

import androidx.annotation.IntDef
import androidx.annotation.Keep
import androidx.annotation.StringDef
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

const val DOWNLOADED = 1
const val IS_NOT_DOWNLOADED = 0

const val EPISODE = 1
const val PODCAST = 2
const val CATEGORY = 3
const val BANNER = 4
const val BUTTON = 5

@Keep
class Status {

    companion object {

        @Target(AnnotationTarget.TYPE, AnnotationTarget.PROPERTY)
        @IntDef(PODCAST, EPISODE, BANNER, CATEGORY, BUTTON)
        @Retention(RetentionPolicy.SOURCE)
        annotation class ExploreViewType

        @Target(AnnotationTarget.TYPE, AnnotationTarget.PROPERTY)
        @IntDef(DOWNLOADED, IS_NOT_DOWNLOADED)
        @Retention(RetentionPolicy.SOURCE)
        annotation class DownloadStatus

        const val NEW = 1
        const val IN_PROGRESS = 2
        const val PLAYED = 3

        const val BEST = "best"
        const val NEWEST = "new"

        @Target(AnnotationTarget.TYPE, AnnotationTarget.PROPERTY)
        @StringDef(BEST, NEWEST)
        @Retention(RetentionPolicy.SOURCE)
        annotation class SortBy
    }
}
