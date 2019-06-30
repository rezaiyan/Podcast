package com.hezaro.wall.feature.player

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.util.Util
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hezaro.wall.R
import com.hezaro.wall.R.drawable
import com.hezaro.wall.data.model.DOWNLOADED
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.IS_NOT_DOWNLOADED
import com.hezaro.wall.feature.main.MainActivity
import com.hezaro.wall.feature.main.SharedViewModel
import com.hezaro.wall.feature.player.utils.SpeedPicker
import com.hezaro.wall.feature.search.PLAY_EPISOD_FROM_PLAYLIST
import com.hezaro.wall.feature.search.PLAY_SINGLE_TRACK
import com.hezaro.wall.feature.search.RESUME_VIEW
import com.hezaro.wall.feature.search.SELECT_SINGLE_TRACK
import com.hezaro.wall.feature.search.UPDATE_VIEW
import com.hezaro.wall.sdk.platform.ext.gone
import com.hezaro.wall.sdk.platform.ext.hide
import com.hezaro.wall.sdk.platform.ext.load
import com.hezaro.wall.sdk.platform.ext.show
import com.hezaro.wall.sdk.platform.ext.toRange
import com.hezaro.wall.sdk.platform.player.MediaPlayerState
import com.hezaro.wall.sdk.platform.player.download.DownloadTracker
import com.hezaro.wall.sdk.platform.player.download.PlayerDownloadHelper
import com.hezaro.wall.sdk.platform.utils.ACTION_PLAY_PAUSE
import com.hezaro.wall.services.MediaPlayerServiceHelper
import com.hezaro.wall.utils.OnSwipeTouchListener
import com.hezaro.wall.utils.RoundRectTransform
import com.minibugdev.drawablebadge.BadgePosition
import com.minibugdev.drawablebadge.DrawableBadge
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import ir.smartlab.persindatepicker.util.PersianCalendar
import kotlinx.android.synthetic.main.fragment_player.closePlayer
import kotlinx.android.synthetic.main.fragment_player.episodeInfo
import kotlinx.android.synthetic.main.fragment_player.logo
import kotlinx.android.synthetic.main.fragment_player.miniPlayer
import kotlinx.android.synthetic.main.fragment_player.miniPlayerProgressBar
import kotlinx.android.synthetic.main.fragment_player.playPause
import kotlinx.android.synthetic.main.fragment_player.playerLayout
import kotlinx.android.synthetic.main.fragment_player.playerView
import kotlinx.android.synthetic.main.fragment_player.speedChooser
import kotlinx.android.synthetic.main.fragment_player.subtitle
import kotlinx.android.synthetic.main.fragment_player.title
import kotlinx.android.synthetic.main.player_control.bookmarkAction
import kotlinx.android.synthetic.main.player_control.descriptionAction
import kotlinx.android.synthetic.main.player_control.downloadAction
import kotlinx.android.synthetic.main.player_control.episodeAvatar
import kotlinx.android.synthetic.main.player_control.episodeDescription
import kotlinx.android.synthetic.main.player_control.episodeShare
import kotlinx.android.synthetic.main.player_control.episodeTitle
import kotlinx.android.synthetic.main.player_control.exo_ffwd
import kotlinx.android.synthetic.main.player_control.exo_rew
import kotlinx.android.synthetic.main.player_control.likeAction
import kotlinx.android.synthetic.main.player_control.likeActionLayout
import kotlinx.android.synthetic.main.player_control.likeCount
import kotlinx.android.synthetic.main.player_control.playerMinimize
import kotlinx.android.synthetic.main.player_control.podcastTitle
import kotlinx.android.synthetic.main.player_control.releaseDate
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.Formatter
import java.util.Locale
import java.util.concurrent.TimeUnit

class PlayerFragment : Fragment(), DownloadTracker.Listener {
    private fun layoutId() = R.layout.fragment_player
    fun tag(): String = this::class.java.simpleName
    private var isBuffering = false
    private var isExpanded = false
    private var currentEpisode: Episode? = null
    private val vm: PlayerViewModel by inject()
    private lateinit var sharedVm: SharedViewModel
    private var behavior: BottomSheetBehavior<View>? = null
    private var showInfo = false

    private val downloadHelper: PlayerDownloadHelper by inject()
    private val downloader by lazy { downloadHelper.getDownloadTracker()!! }
    private val calendar = PersianCalendar()

    companion object {
        fun getInstance() = PlayerFragment()
    }

    private fun onSlide(slideOffset: Float) {
        Timber.i("slideOffset: $slideOffset")
        playerLayout.updateRadius(slideOffset)
        updateMarginList(
            slideOffset.toRange(
                0..1,
                0..(resources.getDimension(R.dimen.mini_player_height).toInt() * -1)
            ).toInt()
        )
        miniPlayer.alpha = slideOffset.toRange(0..1, 1..0)
        if (showInfo && slideOffset < 0.5F) {
            (activity as MainActivity).openEpisodeInfo(currentEpisode!!)
            showInfo = false
        }
    }

    private fun updateMarginList(i: Int = -1) {
        val params = miniPlayer.layoutParams as AppBarLayout.LayoutParams
        params.topMargin = i
        miniPlayer?.requestLayout()
    }

    private fun onStateChanged(newState: Int) {
        sharedVm.updateSheetState(newState)
        sharedVm.playerIsOpen(behavior?.peekHeight!! > 0)

//        if (isBuffering && newState == BottomSheetBehavior.STATE_DRAGGING)
//            behavior?.state = BottomSheetBehavior.STATE_COLLAPSED

        isExpanded = when (newState) {
            BottomSheetBehavior.STATE_EXPANDED -> {
                true
            }
            BottomSheetBehavior.STATE_COLLAPSED -> {
                false
            }
            else -> {
                false
            }
        }
    }

    fun setBehavior() {
        behavior = BottomSheetBehavior.from(this@PlayerFragment.view)
        behavior!!.state = BottomSheetBehavior.STATE_HIDDEN

        playerLayout.setOnTouchListener(
            OnSwipeTouchListener(
                behavior!!, ::onStateChanged, ::onSlide
            ) {
                (activity as MainActivity).unbindService()
                playerView.player.stop()
                MediaPlayerServiceHelper.stopService(requireContext())
                closeMiniPlayer()
            }
        )
    }

    fun setPlayer(player: Player) {

        playerView.player = player

        if (!(activity as MainActivity).serviceIsBounded && (activity as MainActivity).errorOccurred)
            closeMiniPlayer()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(layoutId(), container, false)

    private var progressObservable: Disposable? = null

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedVm = ViewModelProviders.of(requireActivity()).get(SharedViewModel::class.java)
        sharedVm.episode.observe(this, Observer { preparePlayer(it) })
        sharedVm.playStatus.observe(this, Observer<Int> { updatePlayingStatus(it) })
        sharedVm.collapseSheet.observe(this, Observer<Int> { behavior?.state = it })
        sharedVm.isServiceConnected.observe(this, Observer<Boolean> { if (!it) playerView.player = null })

        speedChooser.text = "${vm.defaultSpeed()}x"
        val speedPicker = SpeedPicker(context!!, ::setSpeedListener)
        speedChooser.setOnClickListener {
            speedPicker.show(vm.defaultSpeed())
        }
        episodeInfo.setOnClickListener {
            (activity as MainActivity).openEpisodeInfo(currentEpisode!!)
            collapse()
            showInfo = true
        }
        closePlayer.setOnClickListener { closeMiniPlayer() }

        val formatBuilder = StringBuilder()
        val formatter = Formatter(formatBuilder, Locale.getDefault())

        progressObservable = Observable.interval(1, TimeUnit.SECONDS)
            .map {
                playerView.player?.currentPosition?.div(1000) ?: 0
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { progress ->
                playerView.player?.let {
                    Timber.d("onViewCreated, progress: $progress")
                    miniPlayerProgressBar.maximum = playerView.player.duration.toFloat() / 1000
                    miniPlayerProgressBar.progress = (progress).toFloat()
                    subtitle.text = "${Util.getStringForTime(formatBuilder, formatter, progress * 1000)
                    }/${Util.getStringForTime(formatBuilder, formatter, playerView.player.duration)}"

                }
            }

        episodeShare.setOnClickListener {
            val shareIntent = ShareCompat.IntentBuilder.from(activity)
                .setType("text/plain")
                .setChooserTitle("ارسال اپیزود ${currentEpisode?.title} ")
                .setText("http://wall.hezaro.com/e/${currentEpisode?.id}/")
                .intent
            if (shareIntent.resolveActivity(context!!.packageManager) != null) {
                startActivity(shareIntent)
            }
        }
        playerMinimize.setOnClickListener {
            when (behavior?.state) {
                BottomSheetBehavior.STATE_COLLAPSED -> {

                    if (!isBuffering)
                        behavior?.state = BottomSheetBehavior.STATE_EXPANDED
                }
                BottomSheetBehavior.STATE_EXPANDED -> {
                    progressObservable?.dispose()
                    collapse()
                }
                else -> {
                    progressObservable?.dispose()
                }
            }
        }

        playerLayout.setOnClickListener { behavior?.state = BottomSheetBehavior.STATE_EXPANDED }
        exo_rew.setOnClickListener { MediaPlayerServiceHelper.seekBackward(requireContext()) }
        exo_ffwd.setOnClickListener { MediaPlayerServiceHelper.seekForward(requireContext()) }
        playPause.setOnClickListener { if (!isBuffering) togglePause() }
    }

    private fun initMetaActions() {
        val isLogin = vm.userIsLogin()

        if (isLogin) {
            bookmarkAction.setImageDrawable(
                AppCompatResources.getDrawable(
                    requireContext(),
                    if (currentEpisode!!.isBookmarked) R.drawable.ic_bookmarked_colored else R.drawable.ic_bookmark
                )
            )
            likeAction.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    if (currentEpisode!!.isLiked)
                        R.color.colorDone
                    else
                        R.color.colorAccent
                )
            )
            episodeDescription.gone()
            likeActionLayout.show()
            descriptionAction.show()
            downloadAction.show()
            bookmarkAction.show()
        } else {
            episodeDescription.show()
            likeActionLayout.gone()
            descriptionAction.gone()
            downloadAction.gone()
            bookmarkAction.gone()
        }

        likeActionLayout.setOnClickListener {

            currentEpisode!!.likes++
            currentEpisode!!.isLiked = !currentEpisode!!.isLiked
            vm.sendLikeAction(!currentEpisode!!.isLiked, currentEpisode!!.id)
            sharedVm.notifyEpisode(Pair(UPDATE_VIEW, currentEpisode!!))

            DrawableBadge.Builder(requireContext().applicationContext)
                .drawableResId(R.drawable.ic_action_name)
                .badgeColor(R.color.bg_player_action)
                .badgeSize(R.dimen.badge_size)
                .badgePosition(BadgePosition.BOTTOM_LEFT)
                .textColor(
                    if (currentEpisode!!.isLiked) {
                        R.color.colorDone
                    } else {
                        R.color.colorAccent
                    }
                )
                .showBorder(false)
                .badgeBorderColor(R.color.default_badge_border_color)
                .badgeBorderSize(R.dimen.default_badge_border_size)
                .maximumCounter(99)
                .build()
                .get(currentEpisode!!.likes)
                .let { drawable -> likeCount.setImageDrawable(drawable) }

        }
        downloadAction.setOnClickListener {
            val uri = Uri.parse(currentEpisode!!.source)
            val title = currentEpisode!!.title
            if (downloader.isDownloaded(uri))
                removeDownloadDialog(title, uri)
            else
                downloader.startDownload(activity!!, title, uri)
        }
        episodeDescription.setOnClickListener { }
        descriptionAction.setOnClickListener { }
        bookmarkAction.setOnClickListener { }
    }

    private fun removeDownloadDialog(title: String, uri: Uri) {
        val dialog = Dialog(context!!)
        dialog.setContentView(R.layout.dialog_remove)

        dialog.findViewById<TextView>(R.id.removeDialogTitle).text = "`$title`  از لیست دانلودها حذف شود؟ "

        dialog.findViewById<Button>(R.id.cancelButton)
            .setOnClickListener { dialog.dismiss() }
        dialog.findViewById<Button>(R.id.removeButton).setOnClickListener {
            Intent(Intent.ACTION_VIEW).apply {
                vm.delete(currentEpisode!!)
                downloader.removeDownload(uri, title)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    override fun onDownloadsChanged(isDownload: Boolean) {
        val downloaded = downloader.isDownloaded(Uri.parse(currentEpisode!!.source))
        if (downloaded) {
            currentEpisode!!.downloadStatus = DOWNLOADED
            vm.save(currentEpisode!!)
            downloadAction.setColorFilter(Color.parseColor("#1DC88D"))
        } else {
            currentEpisode!!.downloadStatus = IS_NOT_DOWNLOADED
            vm.delete(currentEpisode!!)
            downloadAction.setColorFilter(Color.parseColor("#F7F4EB"))
        }

        sharedVm.notifyEpisode(Pair(UPDATE_VIEW, currentEpisode!!))
    }

    override fun onStart() {
        downloader.addListener(this)
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        progressObservable?.dispose()
        downloader.removeListener(this)
        currentEpisode?.let {
            vm.sendLastPosition(it.id, playerView.player.currentPosition)
        }
        playerView.player = null
    }

    @SuppressLint("SetTextI18n")
    fun setSpeedListener(it: Float) {
        vm.speed(it)
        speedChooser.text = "${it}x"
        MediaPlayerServiceHelper.changePlaybackSpeed(context!!, it)
    }

    private fun updatePlayingStatus(action: Int) {
        isBuffering = false
        playPause.setImageResource(drawable.ic_play)
        closePlayer.show()
        when (action) {
            MediaPlayerState.STATE_CONNECTING -> {
                isBuffering = true
            }
            MediaPlayerState.STATE_PLAYING -> {
                closePlayer.hide()
                playPause.setImageResource(drawable.ic_pause)
            }
            MediaPlayerState.STATE_ENDED, MediaPlayerState.STATE_IDLE -> {
                closeMiniPlayer()
            }
            MediaPlayerState.STATE_PAUSED -> {
                currentEpisode?.let {
                    if (playerView.player != null)
                        it.state = playerView.player.currentPosition
                    vm.saveLatestEpisode(it)
                }
            }
        }
    }

    private fun preparePlayer(it: Pair<Int, Episode>) {
        currentEpisode = it.second

        if (!(activity as MainActivity).serviceIsBounded) {
            (activity as MainActivity).bindService()
        }


        when (it.first) {
            SELECT_SINGLE_TRACK -> {
                MediaPlayerServiceHelper.selectEpisode(
                    requireContext(),
                    currentEpisode!!
                )
                collapse()
            }
            PLAY_SINGLE_TRACK -> {
                MediaPlayerServiceHelper.playEpisode(
                    requireContext(),
                    currentEpisode!!
                )
                collapse()
            }
            PLAY_EPISOD_FROM_PLAYLIST -> {
                MediaPlayerServiceHelper.selectEpisode(requireContext(), currentEpisode!!)
                collapse()
            }
            UPDATE_VIEW -> {
                vm.updateEpisode(currentEpisode!!)
            }
            RESUME_VIEW -> {
                // I have to do THIS : because playerView doesn't appear, At this time, I don't know why!!
                togglePause()
                togglePause()
                collapse()
            }
        }



        if (playerView.player != null)
            currentEpisode!!.state = playerView.player.currentPosition
        vm.saveLatestEpisode(currentEpisode!!)
        updatePlayerView()
    }

    private fun togglePause() = MediaPlayerServiceHelper.sendIntent(requireContext(), ACTION_PLAY_PAUSE)

    private fun closeMiniPlayer() {
        sharedVm.listMargin(0)
        sharedVm.progressMargin(resources.getDimension(R.dimen.progress_margin_8).toInt())
        behavior?.isHideable = true
        behavior?.state = BottomSheetBehavior.STATE_HIDDEN
        behavior?.peekHeight = 0
        (activity as MainActivity).unbindService()
    }

    @SuppressLint("CheckResult", "SetTextI18n")
    private fun updatePlayerView() {

        behavior?.isHideable = false
        val height = resources.getDimension(R.dimen.mini_player_height).toInt()
        sharedVm.listMargin(height)
        sharedVm.progressMargin(resources.getDimension(R.dimen.progress_margin).toInt())
        behavior?.peekHeight = height

        currentEpisode?.let {
            title.isSelected = true
            episodeTitle.text = it.title
            episodeDescription.text = Html.fromHtml(it.description, FROM_HTML_MODE_LEGACY)
            podcastTitle.text = it.podcast.title
            calendar.timeInMillis = it.getPublishTime()
            releaseDate.text = calendar.persianLongDate
            title.text = it.title
            logo.load(it.cover, transformation = RoundRectTransform())
            episodeAvatar.load(it.cover, transformation = RoundRectTransform(1F))
        }
    }

    private fun collapse() {
        behavior?.state = BottomSheetBehavior.STATE_COLLAPSED
    }
}
