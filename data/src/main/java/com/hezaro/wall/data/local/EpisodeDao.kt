package com.hezaro.wall.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.hezaro.wall.data.model.Episode
import io.reactivex.Flowable

@Dao
interface EpisodeDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveEpisode(episode: Episode)

    @Delete
    fun delete(episode: Episode)

    @Query("UPDATE episode SET isDownloaded = 0 WHERE lastPlayed = 1 AND id = :id ")
    fun update(id: Long): Int

    @Transaction
    fun removeDownloaded(episode: Episode) {

        val result = update(episode.id)

        if (result > 0)
            return
        else delete(episode)
    }

    @Transaction
    open fun deleteIfIsNotLastPlayed(episode: Episode) {
        val e = getLastPlayed()
        e?.forEach {
            if (it.lastPlayed != 1 && it.isDownloaded == 0) {
                delete(it)
            }
        }
        episode.lastPlayed = 1
        episode.creationDate = System.currentTimeMillis()
        saveLastPlayed(episode)
    }

    @Query("SELECT * FROM episode")
    fun getAllEpisodes(): MutableList<Episode>

    @Query("SELECT * FROM episode WHERE isDownloaded = 1")
    fun getDownloadEpisodes(): Flowable<MutableList<Episode>>

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
                delete(it)
        }
        episode.lastPlayed = 1
        episode.creationDate = System.currentTimeMillis()
        saveLastPlayed(episode)
    }

    @Query("SELECT * FROM episode WHERE lastPlayed = 1")
    fun getLastPlayed(): MutableList<Episode>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveLastPlayed(episode: Episode)
}
