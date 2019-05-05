package com.hezaro.wall.sdk.platform.player.download

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import com.google.android.exoplayer2.offline.ActionFile
import com.google.android.exoplayer2.offline.DownloadAction
import com.google.android.exoplayer2.offline.DownloadAction.Deserializer
import com.google.android.exoplayer2.offline.DownloadHelper
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadManager.TaskState
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.offline.ProgressiveDownloadHelper
import com.google.android.exoplayer2.offline.StreamKey
import com.google.android.exoplayer2.offline.TrackKey
import com.google.android.exoplayer2.ui.DefaultTrackNameProvider
import com.google.android.exoplayer2.ui.TrackNameProvider
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util
import com.hezaro.wall.sdk.platform.R
import java.io.File
import java.io.IOException
import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.CopyOnWriteArraySet

class DownloadTracker(
    context: Context,
    actionFile: File,
    vararg deserializers: DownloadAction.Deserializer
) : DownloadManager.Listener {

    private val context: Context = context.applicationContext

    private val trackNameProvider: TrackNameProvider

    private val listeners: CopyOnWriteArraySet<Listener>

    private val trackedDownloadStates: HashMap<Uri, DownloadAction>

    private val actionFile: ActionFile = ActionFile(actionFile)

    private val actionFileWriteHandler: Handler

    /**
     * Listens for changes in the tracked downloads.
     */
    interface Listener {

        /**
         * Called when the tracked downloads changed.
         */
        fun onDownloadsChanged()
    }

    init {
        trackNameProvider = DefaultTrackNameProvider(context.resources)
        listeners = CopyOnWriteArraySet()
        trackedDownloadStates = HashMap()
        val actionFileWriteThread = HandlerThread("DownloadTracker")
        actionFileWriteThread.start()
        actionFileWriteHandler = Handler(actionFileWriteThread.looper)
        loadTrackedActions(
            if (deserializers.isNotEmpty()) deserializers as Array<Deserializer> else DownloadAction.getDefaultDeserializers()
        )
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    private fun isDownloaded(uri: Uri): Boolean {
        return trackedDownloadStates.containsKey(uri)
    }

    fun getOfflineStreamKeys(uri: Uri): List<StreamKey> {
        return if (!trackedDownloadStates.containsKey(uri)) {
            emptyList()
        } else trackedDownloadStates[uri]!!.keys
    }

    fun toggleDownload(activity: Activity, name: String, uri: Uri) {
        if (isDownloaded(uri)) {
            val removeAction = getDownloadHelper(uri).getRemoveAction(Util.getUtf8Bytes(name))
            startServiceWithAction(removeAction)
        } else {
            val helper = StartDownloadDialogHelper(activity, getDownloadHelper(uri), name)
            helper.prepare()
        }
    }

    override fun onInitialized(downloadManager: DownloadManager) {
        // Do nothing.
    }

    override fun onTaskStateChanged(downloadManager: DownloadManager, taskState: TaskState) {
        val action = taskState.action
        val uri = action.uri
        if (action.isRemoveAction && taskState.state == TaskState.STATE_COMPLETED || !action.isRemoveAction && taskState.state == TaskState.STATE_FAILED) {
            // A download has been removed, or has failed. Stop tracking it.
            if (trackedDownloadStates.remove(uri) != null) {
                handleTrackedDownloadStatesChanged()
            }
        }
    }

    override fun onIdle(downloadManager: DownloadManager) {
        // Do nothing.
    }

    // Internal methods

    private fun loadTrackedActions(deserializers: Array<DownloadAction.Deserializer>) {
        try {
            val allActions = actionFile.load(*deserializers)
            for (action in allActions) {
                trackedDownloadStates[action.uri] = action
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to load tracked actions", e)
        }
    }

    private fun handleTrackedDownloadStatesChanged() {
        for (listener in listeners) {
            listener.onDownloadsChanged()
        }
        val actions = trackedDownloadStates.values.toTypedArray()
        actionFileWriteHandler.post {
            try {
                actionFile.store(*actions)
            } catch (e: IOException) {
                Log.e(TAG, "Failed to store tracked actions", e)
            }
        }
    }

    private fun startDownload(action: DownloadAction) {
        if (trackedDownloadStates.containsKey(action.uri)) {
            // This content is already being downloaded. Do nothing.
            return
        }
        trackedDownloadStates[action.uri] = action
        handleTrackedDownloadStatesChanged()
        startServiceWithAction(action)
    }

    private fun startServiceWithAction(action: DownloadAction) {
        DownloadService.startWithAction(context, PlayerDownloadService::class.java, action, true)
    }

    private fun getDownloadHelper(uri: Uri) = ProgressiveDownloadHelper(uri)

    private inner class StartDownloadDialogHelper(
        activity: Activity, private val downloadHelper: DownloadHelper, private val name: String
    ) : DownloadHelper.Callback, DialogInterface.OnClickListener {

        private val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
            .setTitle(R.string.exo_download_description)
            .setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(android.R.string.cancel, null)

        private val dialogView: View

        private val trackKeys: MutableList<TrackKey>

        private val trackTitles: ArrayAdapter<String>

        private val representationList: ListView

        init {

            // Inflate with the builder's context to ensure the correct style is used.
            val dialogInflater = LayoutInflater.from(builder.context)
            dialogView = dialogInflater.inflate(R.layout.start_download_dialog, null)

            trackKeys = ArrayList()
            trackTitles = ArrayAdapter(
                builder.context, android.R.layout.simple_list_item_multiple_choice
            )
            representationList = dialogView.findViewById(R.id.representation_list)
            representationList.choiceMode = ListView.CHOICE_MODE_MULTIPLE
            representationList.adapter = trackTitles
        }

        fun prepare() {
            downloadHelper.prepare(this)
        }

        override fun onPrepared(helper: DownloadHelper) {
            for (i in 0 until downloadHelper.periodCount) {
                val trackGroups = downloadHelper.getTrackGroups(i)
                for (j in 0 until trackGroups.length) {
                    val trackGroup = trackGroups.get(j)
                    for (k in 0 until trackGroup.length) {
                        trackKeys.add(TrackKey(i, j, k))
                        trackTitles.add(trackNameProvider.getTrackName(trackGroup.getFormat(k)))
                    }
                }
            }
            if (!trackKeys.isEmpty()) {
                builder.setView(dialogView)
            }
            builder.create().show()
        }

        override fun onPrepareError(helper: DownloadHelper, e: IOException) {
            Toast.makeText(
                context.applicationContext, R.string.download_start_error, Toast.LENGTH_LONG
            )
                .show()
            Log.e(TAG, "Failed to start download", e)
        }

        override fun onClick(dialog: DialogInterface, which: Int) {
            val selectedTrackKeys = ArrayList<TrackKey>()
            for (i in 0 until representationList.childCount) {
                if (representationList.isItemChecked(i)) {
                    selectedTrackKeys.add(trackKeys[i])
                }
            }
            if (!selectedTrackKeys.isEmpty() || trackKeys.isEmpty()) {
                val downloadAction = downloadHelper.getDownloadAction(Util.getUtf8Bytes(name), selectedTrackKeys)
                startDownload(downloadAction)
            }
        }
    }

    companion object {

        private val TAG = "DownloadTracker"
    }
}
