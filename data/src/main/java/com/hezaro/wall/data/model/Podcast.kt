package com.hezaro.wall.data.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Keep
@Entity(tableName = "podcast", indices = [Index("id", unique = true)])
class Podcast(
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    var id: Long = 0,
    @SerializedName("title")
    var title: String = "",
    @SerializedName("description")
    var description: String = "",
    @SerializedName("website")
    var website: String = "",
    @SerializedName("creator")
    var creator: String = "",
    @SerializedName("episodes_count")
    var episodeCount: Int = 0,
    @SerializedName("cover")
    var cover: String = ""

) : Parcelable {

    constructor(source: Parcel) : this() {
        id = source.readLong()
        title = source.readString()!!
        description = source.readString()!!
        website = source.readString()!!
        creator = source.readString()!!
        episodeCount = source.readInt()
        cover = source.readString()!!
    }

    override fun equals(other: Any?): Boolean {
        other?.let {
            return (it is Podcast) && (it.id == id)
        }

        return super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun describeContents() = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(title)
        parcel.writeString(description)
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