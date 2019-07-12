package com.hezaro.wall.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.hezaro.wall.data.model.DOWNLOADED
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.IS_NOT_DOWNLOADED
import io.reactivex.Flowable

/**
 * This class is a Data Access Object for [Episode] table of Room database
 * This is used in the Room database class @see {com.hezaro.wall.utils.AppDatabase}
 * */
@Dao
interface EpisodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveEpisode(episode: Episode)

    @Delete
    fun delete(episode: Episode)

    @Query("SELECT * FROM episodes WHERE userId = :userId")
    fun getAllEpisodes(userId: String): List<Episode>

    /**
     * Helper function (private)
     * Updates the field of [Episode.downloadStatus]
     * @see {downloadStatus = 0} means --> downloadStatus = [IS_NOT_DOWNLOADED]
     *
     * @param id Is [Episode.id]
     * @param lastPlayed It has default value to using in the where condition
     * @return {[Int] > 0} if any field is updated else {[Int] < 0}
     */
    @Query("UPDATE episodes SET downloadStatus = 0 , creationDate = :updateTime WHERE isLastPlay = :lastPlayed AND id = :id AND userId = :userId ")
    fun updateDownloadStatus(
        userId: String,
        id: Long,
        lastPlayed: Boolean = true,
        updateTime: Long = System.currentTimeMillis()
    ): Int

    /**
     * Updates the fields of [Episode]
     *
     * @param id            Is [Episode.id]
     * @param bookmarked    Is [Episode.isBookmarked]
     * @param likes         Is [Episode.likes]
     * @param isDownloaded  Is [Episode.downloadStatus]
     * @param lastPlayed    Is [Episode.isLastPlay]
     * @param state         Is [Episode.state]
     */
    @Query("UPDATE episodes SET isBookmarked = :bookmarked,likes = :likes,downloadStatus = :isDownloaded,isLastPlay = :lastPlayed,state = :state , creationDate = :updateTime WHERE id = :id ")
    fun updateDownloadStatus(
        id: Long,
        bookmarked: Boolean,
        likes: Long,
        isDownloaded: Int,
        lastPlayed: Boolean,
        state: Long,
        updateTime: Long = System.currentTimeMillis()
    )

    /**
     * Remove the downloaded [Episode]
     * if the [Episode.isLastPlay] is TRUE
     * only updates the [Episode.downloadStatus] field to [IS_NOT_DOWNLOADED]
     * else delete it
     */
    @Transaction
    fun removeDownloaded(userId: String, episode: Episode) {
        if (updateDownloadStatus(userId, episode.id) > 0) {
            return
        } else delete(episode)
    }

    /**
     * @param episode To updating the [Episode.isLastPlay] field to TRUE
     *
     * Updates the [Episode.isLastPlay] status
     * if [Episode] already is downloaded only update it
     * else delete that episodes and replace another one
     */
    @Transaction
    fun updateLastEpisode(userId: String, episode: Episode) {
        deletePlayedEpisodes(if (userId.isEmpty()) "Guest" else userId)

        val it = getLastPlayedEpisode(if (userId.isEmpty()) "Guest" else userId) //
        it?.let {

            it.isLastPlay = false
            it.userId = if (userId.isEmpty()) "Guest" else userId
            saveEpisode(it)

            episode.isLastPlay = true
            episode.userId = if (userId.isEmpty()) "Guest" else userId
            episode.creationDate = System.currentTimeMillis()
            saveEpisode(episode)
        }
            ?: run {
                episode.isLastPlay = true
                episode.userId = if (userId.isEmpty()) "Guest" else userId
                episode.creationDate = System.currentTimeMillis()
                saveEpisode(episode)
            }
    }

    /**
     * @return Delete the [Episode]s that [Episode.isLastPlay] is True
     */
    @Query("DELETE FROM episodes WHERE isLastPlay = :isLastPlay AND downloadStatus = :notDownloaded AND userId = :userId")
    fun deletePlayedEpisodes(userId: String, isLastPlay: Boolean = true, notDownloaded: Int = IS_NOT_DOWNLOADED)

    /**
     * @return A list of [Episode] that [Episode.downloadStatus] is [DOWNLOADED]
     */
    @Query("SELECT * FROM episodes WHERE downloadStatus = :downloadStatus AND userId = :userId")
    fun getDownloadEpisodeList(userId: String, downloadStatus: Int = DOWNLOADED): List<Episode>

    /**
     * @return A list of [Episode] that [Episode.downloadStatus] is [DOWNLOADED]
     */
    @Query("SELECT * FROM episodes WHERE downloadStatus = :downloadStatus AND userId = :userId")
    fun getDownloadEpisodes(userId: String, downloadStatus: Int = DOWNLOADED): Flowable<List<Episode>>

    /**
     * @return An [Episode] that [Episode.isLastPlay] is TRUE
     */
    @Query("SELECT * FROM episodes WHERE isLastPlay = :lastPlayed AND userId = :userId ORDER BY creationDate DESC LIMIT 1")
    fun getLastPlayedEpisode(userId: String, lastPlayed: Boolean = true): Episode?
}
