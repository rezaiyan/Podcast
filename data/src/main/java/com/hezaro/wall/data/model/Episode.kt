package com.hezaro.wall.data.model

import android.os.Parcel
import android.os.Parcelable
import com.hezaro.wall.data.model.Status.Companion.DownloadStatus
import com.hezaro.wall.data.model.Status.Companion.NEW
import com.hezaro.wall.data.model.Status.Companion.NOT_DOWNLOADED
import com.hezaro.wall.data.model.Status.Companion.PlayStatus

class Episode() : Parcelable {
    var id: Int = -1
    var title: String = ""
    var description: String = ""
    var creator: String = ""
    var votes: Int = -1
    var views: Int = -1
    var cover: String = ""
        set(value) {
            if (!value.startsWith("http"))
                field = "http://$value"
        }
    var source: String = ""
//    var duration: String = ""
    var mime_type: String = ""
    var published_at: Long = 0
    var comment_count: Int = -1

    var podcast: Podcast? = null

    var generatedId: String? = null

    var descriptionHtml: String? = null

    var state: Long = 0

    var isPlayable: Boolean = true

    var url: String? = null

    var remoteMediaUrl: String? = null

    var localMediaUrl: String? = null

    var size: Int = 0

    var downloadedSize: Int = 0

    var mimeType: String? = null

    private var mFavorite: Boolean = false

    var updatedAt: Long = 0

    var isManuallyAdded: Boolean = false

    // channel data
    var channelGeneratedId: String? = null

    var channelTitle: String? = null

    var channelArtworkUrl: String? = null

    var channelAuthor: String? = null

    @PlayStatus
    var status = NEW

    @DownloadStatus
    var downloadStatus = NOT_DOWNLOADED

    var isManualDownload = false

    fun isPlayable(isPlayable: Boolean) {
        this.isPlayable = isPlayable
    }

    constructor(parcel: Parcel) : this() {
        id = parcel.readInt()
        title = parcel.readString()
        description = parcel.readString()
        creator = parcel.readString()
        votes = parcel.readInt()
        views = parcel.readInt()
        cover = parcel.readString()
        source = parcel.readString()
        published_at = parcel.readLong()
        comment_count = parcel.readInt()
        podcast = parcel.readParcelable(Podcast::class.java.classLoader)
        generatedId = parcel.readString()
        descriptionHtml = parcel.readString()
        state = parcel.readLong()
        url = parcel.readString()
        remoteMediaUrl = parcel.readString()
        localMediaUrl = parcel.readString()
        size = parcel.readInt()
        downloadedSize = parcel.readInt()
        mimeType = parcel.readString()
        mFavorite = parcel.readByte() != 0.toByte()
        updatedAt = parcel.readLong()
        isManuallyAdded = parcel.readByte() != 0.toByte()
        channelGeneratedId = parcel.readString()
        channelTitle = parcel.readString()
        channelArtworkUrl = parcel.readString()
        channelAuthor = parcel.readString()
        status = parcel.readInt()
        downloadStatus = parcel.readInt()
        isManualDownload = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(podcast?.let { creator } ?: "")
        parcel.writeInt(votes)
        parcel.writeInt(views)
        parcel.writeString(cover)
        parcel.writeString(source)
        parcel.writeLong(published_at)
        parcel.writeInt(comment_count)
        parcel.writeParcelable(podcast, flags)
        parcel.writeString(generatedId)
        parcel.writeString(descriptionHtml)
        parcel.writeLong(state)
        parcel.writeString(url)
        parcel.writeString(source)
        parcel.writeString(localMediaUrl)
        parcel.writeInt(size)
        parcel.writeInt(downloadedSize)
        parcel.writeString(mimeType)
        parcel.writeByte(if (mFavorite) 1 else 0)
        parcel.writeLong(updatedAt)
        parcel.writeByte(if (isManuallyAdded) 1 else 0)
        parcel.writeString(channelGeneratedId)
        parcel.writeString(channelTitle)
        parcel.writeString(cover)
        parcel.writeString(channelAuthor)
        parcel.writeInt(status)
        parcel.writeInt(downloadStatus)
        parcel.writeByte(if (isManualDownload) 1 else 0)
    }

    override fun describeContents() = 0

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Episode> = object : Parcelable.Creator<Episode> {
            override fun createFromParcel(source: Parcel): Episode = Episode(source)
            override fun newArray(size: Int): Array<Episode?> = arrayOfNulls(size)
        }
    }
}
