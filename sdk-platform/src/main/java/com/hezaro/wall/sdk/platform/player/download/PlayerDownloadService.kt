package com.hezaro.wall.sdk.platform.player.download

import android.app.Notification
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadManager.TaskState
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.scheduler.PlatformScheduler
import com.google.android.exoplayer2.ui.DownloadNotificationUtil
import com.google.android.exoplayer2.util.NotificationUtil
import com.google.android.exoplayer2.util.Util
import com.hezaro.wall.sdk.platform.R

/** A service for downloading media.  */
class PlayerDownloadService : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DownloadService.DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    CHANNEL_ID,
    R.string.exo_download_notification_channel_name
) {

    override fun getDownloadManager(): DownloadManager {
        return PlayerDownloadHelper(applicationContext).getDownloadManager()!!
    }

    override fun getScheduler(): PlatformScheduler? {
        return if (Util.SDK_INT >= 21) PlatformScheduler(this, JOB_ID) else
        /* contentIntent= */ null
    }

    override fun getForegroundNotification(taskStates: Array<TaskState>?): Notification {
        return DownloadNotificationUtil.buildProgressNotification(
            this,
            R.drawable.exo_controls_play,
            CHANNEL_ID, null,
            "Downloading episode",
            taskStates!!
        )/* contentIntent= */
    }

    override fun onTaskStateChanged(taskState: TaskState?) {
        if (taskState!!.action.isRemoveAction) {
            return
        }
        var notification: Notification? = null
        if (taskState.state == TaskState.STATE_COMPLETED) {
            notification = DownloadNotificationUtil.buildDownloadCompletedNotification(
                /* context= */ this,
                R.drawable.exo_controls_play,
                CHANNEL_ID, null,
                Util.fromUtf8Bytes(taskState.action.data)
            )/* contentIntent= */
        } else if (taskState.state == TaskState.STATE_FAILED) {
            notification = DownloadNotificationUtil.buildDownloadFailedNotification(
                /* context= */ this,
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
