package com.hezaro.wall.data.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.hezaro.wall.data.model.Status.Companion.NEW

@Keep
@Entity(tableName = "episodes", indices = [Index("id", unique = true)])
class Episode(
    @SerializedName("userId")
    var userId: String = "Guest",
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    var id: Long = 0,
    @SerializedName("title")
    var title: String = "",
    @SerializedName("description")
    var description: String = "",
    @SerializedName("creator")
    var creator: String = "",
    @SerializedName("likes_count")
    var likes: Int = 0,
    @SerializedName("views_count")
    var views: Long = 0,
    @SerializedName("is_liked")
    var isLiked: Boolean = false,
    @SerializedName("is_bookmarked")
    var isBookmarked: Boolean = false,
    @SerializedName("cover")
    var cover: String = "",
    @SerializedName("source")
    var source: String = "",
    @SerializedName("state")
    var state: Long = 0,
    @SerializedName("mime_type")
    var mimeType: String = "",
    @SerializedName("published_at")
    var publishedTime: Long = 0,
    @SerializedName("comments_count")
    var commentCount: Long = 0,
    @SerializedName("podcast")
    var podcast: Podcast = Podcast(),
    var isLastPlay: Boolean = false,
    var downloadStatus: Int = IS_NOT_DOWNLOADED,
    var playStatus: Int = NEW,
    var creationDate: Long = 0
) : Parcelable {

    private fun formatLongNumber(value: Long): String {
        return when {
            value <= 999 -> value.toString()
            // thousands
            value in 1000..999999 -> "${(value / 1000)}K"
            // millions
            value in 1000000..999999999 -> "${(value / 1000000)}M"
            // billions
            value in 1000000000..999999999999 -> "${(value / 1000000000)}B"
            else -> value.toString()
        }
    }

    fun getPublishTime(): Long {
        return if (publishedTime.toString().length == 10)
            return publishedTime * 1000
        else publishedTime
    }

    fun getFormattedDuration() = formatSeconds(0)
    fun getView() = formatLongNumber(views)
    fun getLike() = formatLongNumber(likes.toLong())

    private fun formatSeconds(timeInSeconds: Int): String {
        if (timeInSeconds > 0) {
            val hours = timeInSeconds / 3600
            val secondsLeft = timeInSeconds - hours * 3600
            val minutes = secondsLeft / 60
            val seconds = secondsLeft - minutes * 60

            var formattedTime = ""
            if (hours < 10)
                formattedTime += "0"
            formattedTime += "$hours:"

            if (minutes < 10)
                formattedTime += "0"
            formattedTime += "$minutes:"

            if (seconds < 10)
                formattedTime += "0"
            formattedTime += seconds

            return formattedTime
        } else return ""
    }

    constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        title = parcel.readString()!!
        description = parcel.readString()!!
        creator = parcel.readString()!!
        likes = parcel.readInt()
        views = parcel.readLong()
        isLiked = parcel.readBoolean()
        isBookmarked = parcel.readBoolean()
        cover = parcel.readString()!!
        source = parcel.readString()!!
        state = parcel.readLong()
        mimeType = parcel.readString()!!
        publishedTime = parcel.readLong()
        commentCount = parcel.readLong()
        podcast = parcel.readParcelable(Podcast::class.java.classLoader)!!
        isLastPlay = parcel.readBoolean()
        downloadStatus = parcel.readInt()
        playStatus = parcel.readInt()
        creationDate = parcel.readLong()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(creator)
        parcel.writeInt(likes)
        parcel.writeLong(views)
        parcel.writeBoolean(isLiked)
        parcel.writeBoolean(isBookmarked)
        parcel.writeString(cover)
        parcel.writeString(source)
        parcel.writeLong(state)
        parcel.writeString(mimeType)
        parcel.writeLong(publishedTime)
        parcel.writeLong(commentCount)
        parcel.writeParcelable(podcast, flags)
        parcel.writeBoolean(isLastPlay)
        parcel.writeInt(downloadStatus)
        parcel.writeInt(playStatus)
        parcel.writeLong(creationDate)
    }

    override fun describeContents() = 0
    fun update(e: Episode) {
        e.let {
            description = it.description
            creator = it.creator
            likes = it.likes
            views = it.views
            isLiked = it.isLiked
            isBookmarked = it.isBookmarked
            cover = it.cover
            source = it.source
            state = it.state
            mimeType = it.mimeType
            publishedTime = it.publishedTime
            commentCount = it.commentCount
            podcast = it.podcast
            isLastPlay = it.isLastPlay
            downloadStatus = it.downloadStatus
            playStatus = it.playStatus
            creationDate = it.creationDate
        }
    }

    override fun equals(other: Any?): Boolean {
        other?.let {
            return (it is Episode) && (it.id == id)
        }

        return super.equals(other)
    }

    companion object {
        @JvmField
        var CREATOR: Parcelable.Creator<Episode> = object : Parcelable.Creator<Episode> {
            override fun createFromParcel(source: Parcel): Episode = Episode(source)
            override fun newArray(size: Int): Array<Episode?> = arrayOfNulls(size)
        }
    }
}
