package com.hezaro.wall.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hezaro.wall.data.model.Podcast

@Dao
interface PodcastDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveEpisode(podcast: Podcast)

    @Delete
    fun delete(podcast: Podcast)

    @Query("SELECT * FROM podcast")
    fun getAll(): List<Podcast>
}