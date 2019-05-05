package com.hezaro.wall.sdk.platform.player.download

import android.content.Context
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloaderConstructorHelper
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.FileDataSourceFactory
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import com.hezaro.wall.sdk.platform.R
import java.io.File

class PlayerDownloadHelper(context: Context) {

    private val context: Context = context.applicationContext

    private val userAgent: String = Util.getUserAgent(context, context.getString(R.string.app_name))

    private var downloadDirectory: File? = null

    private var dlManager: DownloadManager? = null

    private var dlTracker: DownloadTracker? = null

    /**
     * Returns a [DataSource.Factory].
     */
    fun buildDataSourceFactory(): DataSource.Factory {
        val upstreamFactory = DefaultDataSourceFactory(context, buildHttpDataSourceFactory())
        return buildReadOnlyCacheDataSource(upstreamFactory, getDownloadCache())
    }

    /**
     * Returns a [HttpDataSource.Factory].
     */
    private fun buildHttpDataSourceFactory(): HttpDataSource.Factory {
        return DefaultHttpDataSourceFactory(userAgent)
    }

    fun getDownloadManager(): DownloadManager? {
        initDownloadManager()
        return dlManager
    }

    fun getDownloadTracker(): DownloadTracker? {
        initDownloadManager()
        return dlTracker
    }

    @Synchronized
    private fun initDownloadManager() {
        if (dlManager == null) {
            val downloaderConstructorHelper =
                DownloaderConstructorHelper(getDownloadCache(), buildHttpDataSourceFactory())
            dlManager = DownloadManager(
                downloaderConstructorHelper,
                MAX_SIMULTANEOUS_DOWNLOADS,
                DownloadManager.DEFAULT_MIN_RETRY_COUNT,
                File(getDownloadDirectory(), DOWNLOAD_ACTION_FILE)
            )
            dlTracker = DownloadTracker(
                context,
                File(getDownloadDirectory(), DOWNLOAD_TRACKER_ACTION_FILE)
            )
            dlManager!!.addListener(dlTracker)
        }
    }

    @Synchronized
    private fun getDownloadCache(): Cache {
        if (downloadCache == null) {
            val downloadContentDirectory = File(getDownloadDirectory(), DOWNLOAD_CONTENT_DIRECTORY)
            downloadCache = SimpleCache(downloadContentDirectory, NoOpCacheEvictor())
        }
        return downloadCache!!
    }

    private fun getDownloadDirectory(): File? {
        if (downloadDirectory == null) {
            downloadDirectory = context.getExternalFilesDir(null)
            if (downloadDirectory == null) {
                downloadDirectory = context.filesDir
            }
        }

        return downloadDirectory
    }

    private fun buildReadOnlyCacheDataSource(
        upstreamFactory: DefaultDataSourceFactory, cache: Cache
    ): CacheDataSourceFactory {
        return CacheDataSourceFactory(
            cache,
            upstreamFactory,
            FileDataSourceFactory(), null,
            CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR, null
        )
    }

    companion object {

        private val DOWNLOAD_ACTION_FILE = "actions"

        private val DOWNLOAD_TRACKER_ACTION_FILE = "tracked_actions"

        private val DOWNLOAD_CONTENT_DIRECTORY = "wallpodcast"

        private val MAX_SIMULTANEOUS_DOWNLOADS = 2

        private var downloadCache: Cache? = null
    }
}
