package com.hezaro.wall.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Podcast

/**
 * To converting some specific fields for [Episode] table we need to having it
 * This is used in the Room database class @see {com.hezaro.wall.utils.AppDatabase}
 * */
class EpisodeConverter {


    companion object {

        /**
         * Convert the [Podcast] into a [String] as a json
         *
         * @param podcast     It's a [Podcast] object
         * @return [String]   It's a json to be injectable to the table
         */
        @TypeConverter
        @JvmStatic
        fun toJson(podcast: Podcast): String {
            return Gson().toJson(podcast)
        }

        /**
         * Convert the [String] as a json into a [Podcast]
         *
         * @param json     It's a json that already injected into the table
         * @return [Podcast]   It's a object that converted from a json
         */
        @TypeConverter
        @JvmStatic
        fun toPodcast(json: String): Podcast {
            val type = object : TypeToken<Podcast>() {}.type
            return Gson().fromJson(json, type)
        }
    }
}
