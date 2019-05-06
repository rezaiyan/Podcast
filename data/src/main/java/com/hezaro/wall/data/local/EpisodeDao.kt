package com.hezaro.wall.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.hezaro.wall.data.model.Episode

@Dao
interface EpisodeDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveEpisode(episode: Episode)

    @Delete
    fun delete(episode: Episode)

    @Query("SELECT * FROM episode WHERE lastPlayed = 1 ORDER BY creationDate DESC LIMIT 1")
    fun getLastPlayedEpisode(): Episode?

    @Transaction
    open fun updateLastEpisode(episode: Episode) {
        val e = getLastPlayed()
        e?.forEach {
            if (it.isDownloaded == 1) {
                it.lastPlayed = 0
                saveEpisode(it)
            } else
                deleteLastPlayed(it.id)
        }
        episode.lastPlayed = 1
        episode.creationDate = System.currentTimeMillis()
        saveLastPlayed(episode)
    }

    @Query("SELECT * FROM episode WHERE lastPlayed = 1")
    fun getLastPlayed(): MutableList<Episode>?

    @Query("DELETE  FROM episode WHERE id = :id")
    fun deleteLastPlayed(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveLastPlayed(episode: Episode)
}
