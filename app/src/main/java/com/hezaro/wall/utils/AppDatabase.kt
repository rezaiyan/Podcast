package com.hezaro.wall.utils

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hezaro.wall.data.local.EpisodeConverter
import com.hezaro.wall.data.local.EpisodeDao
import com.hezaro.wall.data.local.PodcastDao
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Podcast

@Database(entities = [Episode::class, Podcast::class], version = 1, exportSchema = false)
@TypeConverters(EpisodeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun episodeDao(): EpisodeDao

    abstract fun podcastDao(): PodcastDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "wall").build()
    }
}
