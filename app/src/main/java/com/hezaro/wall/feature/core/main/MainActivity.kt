package com.hezaro.wall.feature.core.main

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.ProgressBar
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.hezaro.wall.R
import com.hezaro.wall.R.string
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Playlist
import com.hezaro.wall.data.model.Podcast
import com.hezaro.wall.data.model.UserInfo
import com.hezaro.wall.data.model.Version
import com.hezaro.wall.feature.core.player.PlayerFragment
import com.hezaro.wall.feature.episode.EpisodeFragment
import com.hezaro.wall.feature.explore.ExploreFragment
import com.hezaro.wall.feature.podcast.PodcastFragment
import com.hezaro.wall.feature.profile.ProfileFragment
import com.hezaro.wall.feature.search.SearchFragment
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.platform.BaseActivity
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.sdk.platform.ext.load
import com.hezaro.wall.sdk.platform.player.MediaPlayerState
import com.hezaro.wall.services.MediaPlayerService
import com.hezaro.wall.services.MediaPlayerServiceHelper
import com.hezaro.wall.utils.ACTION_EPISODE
import com.hezaro.wall.utils.ACTION_EPISODE_GET
import com.hezaro.wall.utils.ACTION_PLAYER
import com.hezaro.wall.utils.ACTION_PLAYER_STATUS
import com.hezaro.wall.utils.CircleTransform
import com.hezaro.wall.utils.RC_SIGN_IN
import kotlinx.android.synthetic.main.activity_layout.progressBar
import kotlinx.android.synthetic.main.fragment_player.playerView
import kotlinx.android.synthetic.main.toolbar.profile
import org.koin.android.ext.android.inject
import timber.log.Timber

class MainActivity : BaseActivity() {

    private val exploreFragment by lazy { ExploreFragment.getInstance() }
    private val profileFragment by lazy { ProfileFragment.getInstance() }

    override fun fragment() = exploreFragment

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val vm: MainViewModel by inject()
    override fun layoutId() = R.layout.activity_layout
    override fun progressBar(): ProgressBar = progressBar
    override fun fragmentContainer() = R.id.fragmentContainer
    private val playerFragment: PlayerFragment by lazy { (supportFragmentManager?.findFragmentById(R.id.playerFragment) as PlayerFragment?)!! }

    var updateEpisode: MutableLiveData<Episode> = MutableLiveData()

    var playerService: MediaPlayerService? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            playerView.player = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            playerService = (service as MediaPlayerService.ServiceBinder).service

            val currentEpisode = playerService?.currentEpisode
            if (currentEpisode != null) {
                playerFragment.updateMiniPlayer(currentEpisode)
            } else {
                playerService?.serviceConnected()
            }
            playerView.player = playerService?.player
            playerService?.mediaPlayer?.setPlaybackSpeed(vm.defaultSpeed())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId())
        startService(Intent(this, MediaPlayerService::class.java))

        with(vm) {
            observe(login, ::onLogin)
            observe(version, ::onVersion)
            observe(episode, ::onLoadLastPlayedEpisode)
            failure(failure, ::onFailure)
        }

        prepareGoogleSignIn()
    }

    private fun onLoadLastPlayedEpisode(episode: Episode) {
        playerFragment.onLoadLastPlayedEpisode(episode)
    }

    private fun bindService() {
        bindService(Intent(this, MediaPlayerService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun search() {
        playerFragment.collapse(); addFragment(SearchFragment())
    }

    fun openEpisodeInfo(episode: Episode) {
        addFragment(EpisodeFragment.newInstance(episode))
    }

    fun openPodcastInfo(p: Podcast) {
        addFragment(PodcastFragment.newInstance(p))
    }

    fun profile() {
        if (GoogleSignIn.getLastSignedInAccount(this) == null) {
            signIn()
        } else {
            playerFragment.collapse();addFragment(profileFragment)
        }
    }

    private fun prepareGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(string.server_client_id))
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        unbindService(serviceConnection)
    }

    override fun onStart() {
        super.onStart()
        bindService()
        updateUI(GoogleSignIn.getLastSignedInAccount(this))
        val iff = IntentFilter(ACTION_EPISODE)
        iff.addAction(ACTION_PLAYER)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, iff)
    }

    private fun signIn() {
        showProgress()
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            onLoginResult(task)
        }
    }

    private fun onLoginResult(completedTask: Task<GoogleSignInAccount>) {
        hideProgress()
        try {
            val account = completedTask.getResult(ApiException::class.java)

            // Signed in successfully, show authenticated UI.
            updateUI(account)
        } catch (e: ApiException) {
            // The ApiException playStatus code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class listenerReference for more information.
            Timber.w("signInResult:failed code=" + e.statusCode)
            updateUI(null)
        }
    }

    private fun updateUI(account: GoogleSignInAccount?) {

        account?.let {
            Timber.i("idToken== ${it.idToken}")
            vm.login(it.idToken!!)
        }
    }

    private fun onLogin(it: UserInfo) {
        profile.load(it.avatar, transformation = CircleTransform())
    }

    private fun onVersion(it: Version) {
        if (!it.force_update)
            forceUpdateDialog()
    }

    private fun onFailure(failure: Failure) {
        when (failure) {
            is Failure.UserNotFound -> mGoogleSignInClient.signOut()
        }
    }

    override fun onBackPressed() {
        if (playerFragment.isExpand())
            playerFragment.collapse()
        else {
            (supportFragmentManager.findFragmentById(fragmentContainer()) as BaseFragment).onBackPressed()
            super.onBackPressed()
        }
    }

    private var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent != null) {

                when (intent.action) {
                    ACTION_EPISODE -> {
                        val episode = intent.getParcelableExtra<Episode>(ACTION_EPISODE_GET)
                        playerFragment.updateEpisodeView(episode)
                        exploreFragment.updateEpisodeView(episode)
                    }
                    ACTION_PLAYER -> {
                        val action = intent.getIntExtra(ACTION_PLAYER_STATUS, MediaPlayerState.STATE_IDLE)
                        playerFragment.updatePlayingStatus(action)
                    }
                }
            }
        }
    }

    fun isPlayerExpand() = playerFragment.isExpand()

    fun isPlayerOpen() = playerFragment.isOpen()

    fun playEpisode(episode: Episode) = playerFragment.openMiniPlayer(episode)

    fun preparePlaylist(
        playlist: Playlist,
        isLoadMore: Boolean = false
    ) {
        if (!isLoadMore)
            MediaPlayerServiceHelper.clearPlaylist(this)

        MediaPlayerServiceHelper.preparePlaylist(this, playlist)
    }

    fun prepareAndPlayPlaylist(
        playlist: Playlist, e: Episode
    ) {
        MediaPlayerServiceHelper.prepareAndPlayPlaylist(this, playlist, e)
    }

    fun finishFragment(tag: String, playlistCreated: Boolean) {
        if (tag == "ProfileFragment")
            exploreFragment.onRestore(playlistCreated)
    }
}
