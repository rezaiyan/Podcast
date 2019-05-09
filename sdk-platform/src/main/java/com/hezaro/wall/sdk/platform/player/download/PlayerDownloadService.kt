package com.hezaro.wall.sdk.platform.player.download

import android.app.Notification
import com.google.android.exoplayer2.offline.DownloadManager.TaskState
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.scheduler.PlatformScheduler
import com.google.android.exoplayer2.ui.DownloadNotificationUtil
import com.google.android.exoplayer2.util.NotificationUtil
import com.google.android.exoplayer2.util.Util
import com.hezaro.wall.sdk.platform.R
import org.koin.android.ext.android.inject

/** A service for downloading media.  */
class PlayerDownloadService : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DownloadService.DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    CHANNEL_ID,
    R.string.exo_download_notification_channel_name
) {

    private val downloadHelper: PlayerDownloadHelper by inject()

    override fun getDownloadManager() = downloadHelper.getDownloadManager()!!
    override fun getScheduler() = PlatformScheduler(this, JOB_ID)

    override fun getForegroundNotification(taskStates: Array<TaskState>?): Notification {
        return DownloadNotificationUtil.buildProgressNotification(
            this,
            R.drawable.exo_controls_play,
            CHANNEL_ID, null,
            getString(R.string.downloading),
            taskStates!!
        )
    }

    override fun onTaskStateChanged(taskState: TaskState?) {
        if (taskState!!.action.isRemoveAction) {
            return
        }
        var notification: Notification? = null
        if (taskState.state == TaskState.STATE_COMPLETED) {
            notification = DownloadNotificationUtil.buildDownloadCompletedNotification(
                this,
                R.drawable.exo_controls_play,
                CHANNEL_ID, null,
                Util.fromUtf8Bytes(taskState.action.data)
            )
        } else if (taskState.state == TaskState.STATE_FAILED) {
            notification = DownloadNotificationUtil.buildDownloadFailedNotification(
                this,
                R.drawable.exo_controls_play,
                CHANNEL_ID, null,
                Util.fromUtf8Bytes(taskState.action.data)
            )
        }
        val notificationId = FOREGROUND_NOTIFICATION_ID + 1 + taskState.taskId
        NotificationUtil.setNotification(this, notificationId, notification)
    }

    companion object {

        private val CHANNEL_ID = "download_channel"
        private val JOB_ID = 1
        private val FOREGROUND_NOTIFICATION_ID = 1
    }
}
