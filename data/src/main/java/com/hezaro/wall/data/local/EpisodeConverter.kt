package com.hezaro.wall.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hezaro.wall.data.model.Podcast

class EpisodeConverter {

    companion object {

        @TypeConverter
        @JvmStatic
        fun toJson(podcast: Podcast): String {
            return Gson().toJson(podcast)
        }

        @TypeConverter
        @JvmStatic
        fun toPodcast(json: String): Podcast {
            val type = object : TypeToken<Podcast>() {}.type
            return Gson().fromJson(json, type)
        }
    }
}
