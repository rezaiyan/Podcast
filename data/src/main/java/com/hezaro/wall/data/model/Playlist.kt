package com.hezaro.wall.data.model

import android.os.Parcel

class Playlist(private val list: ArrayList<Episode>) : KParcelable {

    fun getItems() = list
    fun getFirst() = list[0]
    fun getItem(index: Int) = list[index]
    fun getIndex(episode: Episode): Int {
        var i = -1
        list.forEachIndexed { index, it -> if (it.id == episode.id) i = index }
        return i
    }

    private constructor(p: Parcel) : this(list = p.readArrayList(Episode::class.java.classLoader) as ArrayList<Episode>)

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeList(list)
    }

    companion object {
        @JvmField
        val CREATOR = parcelableCreator(::Playlist)
    }
}