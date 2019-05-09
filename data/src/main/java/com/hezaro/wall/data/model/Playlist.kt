package com.hezaro.wall.data.model

import android.os.Parcel

class Playlist(private val list: ArrayList<Episode>) : KParcelable {

    fun getItems() = list
    fun addItem(episode: Episode) = list.add(episode)
    fun getItem(index: Int) = list[index]
    fun getIndex(episode: Episode): Int {
        var i = -1
        list.forEachIndexed { index, it -> if (it.id == episode.id) i = index }
        return i
    }

    override fun equals(other: Any?): Boolean {
        return if (other is Playlist && getItems().size > 0 && other.getItems().size > 0 && getItems()[0] == other.getItems()[0])
            true
        else
            super.equals(other)
    }

    override fun hashCode(): Int {
        return if (getItems().size > 0)
            getItems().size * 100
        else
            super.hashCode()
    }

    private constructor(p: Parcel) : this(list = p.readArrayList(Episode::class.java.classLoader) as ArrayList<Episode>)

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeList(list)
    }

    fun diff(playlist: Playlist): Playlist {
        val diff = mutableListOf<Episode>()
        playlist.getItems().forEach { that ->
            getItems().forEach {
                if (that != it)
                    diff.add(that)
            }
        }
        return Playlist(ArrayList(diff))
    }

    companion object {
        @JvmField
        val CREATOR = parcelableCreator(::Playlist)
    }
}