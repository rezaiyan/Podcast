package com.hezaro.wall.data.model

import android.os.Parcel
import android.os.Parcelable

class Podcast : Parcelable {

    var id: Int = -1

    var episodes_count: Int = -1

    var title: String? = ""

    var cover: String? = ""

    var website: String? = ""

    var creator: String? = ""

    constructor(source: Parcel) {
        id = source.readInt()
        episodes_count = source.readInt()
        title = source.readString()
        cover = source.readString()
        website = source.readString()
        creator = source.readString()
    }

    override fun describeContents() = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(episodes_count)
        parcel.writeString(title)
        parcel.writeString(cover)
        parcel.writeString(website)
        parcel.writeString(creator)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Podcast> = object : Parcelable.Creator<Podcast> {
            override fun createFromParcel(source: Parcel): Podcast = Podcast(source)
            override fun newArray(size: Int): Array<Podcast?> = arrayOfNulls(size)
        }
    }
}