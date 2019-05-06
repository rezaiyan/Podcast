package com.hezaro.wall.data.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class Podcast(
    var id: Int = 0,
    var title: String = "",
    var website: String = "",
    var creator: String = "",
    @SerializedName("episodes_count")
    var episodeCount: Int = 0,
    var cover: String = ""

) : Parcelable {

    constructor(source: Parcel) : this() {
        id = source.readInt()
        title = source.readString()!!
        website = source.readString()!!
        creator = source.readString()!!
        episodeCount = source.readInt()
        cover = source.readString()!!
    }

    override fun describeContents() = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(website)
        parcel.writeString(creator)
        parcel.writeInt(episodeCount)
        parcel.writeString(cover)
    }

    companion object {
        @JvmField
        var CREATOR: Parcelable.Creator<Podcast> = object : Parcelable.Creator<Podcast> {
            override fun createFromParcel(source: Parcel): Podcast = Podcast(source)
            override fun newArray(size: Int): Array<Podcast?> = arrayOfNulls(size)
        }
    }
}