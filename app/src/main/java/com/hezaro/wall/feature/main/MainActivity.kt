package com.hezaro.wall.feature.main

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.widget.ProgressBar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.hezaro.wall.R
import com.hezaro.wall.R.string
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Podcast
import com.hezaro.wall.data.model.UserInfo
import com.hezaro.wall.feature.episode.EpisodeFragment
import com.hezaro.wall.feature.episodes.EpisodesFragment
import com.hezaro.wall.feature.explore.ExploreFragment
import com.hezaro.wall.feature.player.PlayerFragment
import com.hezaro.wall.feature.podcast.PodcastFragment
import com.hezaro.wall.feature.profile.ProfileFragment
import com.hezaro.wall.feature.search.RESUME_VIEW
import com.hezaro.wall.feature.search.SELECT_SINGLE_TRACK
import com.hezaro.wall.feature.search.SearchFragment
import com.hezaro.wall.feature.search.UPDATE_VIEW
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.platform.BaseActivity
import com.hezaro.wall.sdk.platform.player.MediaPlayerState
import com.hezaro.wall.sdk.platform.utils.ACTION_EPISODE
import com.hezaro.wall.sdk.platform.utils.ACTION_EPISODE_GET
import com.hezaro.wall.sdk.platform.utils.ACTION_PLAYER
import com.hezaro.wall.sdk.platform.utils.ACTION_PLAYER_STATUS
import com.hezaro.wall.sdk.platform.utils.ERROR_LOGIN_CODE
import com.hezaro.wall.sdk.platform.utils.RC_SIGN_IN
import com.hezaro.wall.services.MediaPlayerService
import com.hezaro.wall.services.MediaPlayerServiceHelper
import kotlinx.android.synthetic.main.activity_main.progressBar
import org.koin.android.ext.android.inject
import timber.log.Timber

class MainActivity : BaseActivity() {

    override fun fragment() = ExploreFragment.getInstance()

    private var gso: GoogleSignInOptions? = null
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val vm: MainViewModel by inject()
    private lateinit var sharedVm: SharedViewModel
    override fun layoutId() = R.layout.activity_main
    override fun progressBar(): ProgressBar = progressBar
    override fun fragmentContainer() = R.id.fragmentContainer
    var serviceIsBounded = false
    var errorOccurred = false
    private var loginRequest = false
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            serviceIsBounded = false
            sharedVm.serviceConnection(false)
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

            val playerService = (service as MediaPlayerService.ServiceBinder).service
            (supportFragmentManager.findFragmentById(R.id.playerFragment) as PlayerFragment).setPlayer(playerService.mediaPlayer.player)

            playerService.player.observe(this@MainActivity, Observer {
                (supportFragmentManager.findFragmentById(R.id.playerFragment) as PlayerFragment).setPlayer(
                    playerService.mediaPlayer.player
                )

            })
            playerService.liveError.observe(this@MainActivity, Observer {
                errorOccurred = it.first

            })

            val currentEpisode = playerService.currentEpisode
            if (currentEpisode != null) {
                sharedVm.isPlaying(true)
                sharedVm.notifyEpisode(Pair(RESUME_VIEW, currentEpisode))
            }

            sharedVm.serviceConnection(true)
            playerService.mediaPlayer.setPlaybackSpeed(vm.defaultSpeed())

            serviceIsBounded = true
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private var comFromDeepLink = false
    private fun handleIntent(intent: Intent?) {
        intent?.let {
            val appLinkAction = intent.action
            val appLinkData: Uri? = intent.data
            if (Intent.ACTION_VIEW == appLinkAction) {
                appLinkData?.let { uri ->
                    uri.path?.let { path ->
                        uri.lastPathSegment?.let {
                            if (path.contains("e")) {
                                comFromDeepLink = true
                                showProgress()
                                with(vm) {
                                    observe(deepEpisode, ::openEpisodeInfo)
                                    getEpisode(it.toLong())

                                }
                            } else if (path.contains("p")) {
                                showProgress()
                                comFromDeepLink = true
                                with(vm) {
                                    observe(deepPodcast, ::openPodcastInfo)
                                    getPodcast(it.toLong())

                                }
                            }
                        }
                    }
                }
                Timber.d("appLinkData?.path == ${appLinkData?.path}")
                Timber.d("appLinkData?.lastPathSegment == ${appLinkData?.lastPathSegment}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId())
        handleIntent(intent)
        sharedVm = ViewModelProviders.of(this).get(SharedViewModel::class.java)

        (supportFragmentManager.findFragmentById(R.id.playerFragment) as PlayerFragment).setBehavior()
        sharedVm.progressMargin.observe(this, Observer { progressbarMargin(it) })


        startService(Intent(this, MediaPlayerService::class.java))
        with(vm) {
            observe(login, ::onLogin)
            observe(episode, ::onLatestEpisode)
            failure(failure, ::onFailure)
        }

        prepareGoogleSignIn()

        val userInfo = vm.userInfo()
        if (userInfo.email.isNotEmpty()) {
            onLogin(userInfo)
        }
    }

    fun search() {
        sharedVm.collapsePlayer()
        addFragment(SearchFragment.newInstance())
    }

    fun profile() {
        if (GoogleSignIn.getLastSignedInAccount(this) == null) {
            loginRequest = true
            showProgress()
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        } else {
            sharedVm.collapsePlayer()
            addFragment(ProfileFragment.getInstance())
        }
    }

    private fun prepareGoogleSignIn() {
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(string.server_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso!!)
    }

    fun bindService() {
        serviceIsBounded = true
        bindService(Intent(this, MediaPlayerService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbindService() {
        if (serviceIsBounded) {
            unbindService(serviceConnection)
            serviceIsBounded = false
        }
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        unbindService()
    }

    override fun onStart() {
        super.onStart()
        bindService()
        val iff = IntentFilter(ACTION_EPISODE)
        iff.addAction(ACTION_PLAYER)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, iff)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val completedTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            hideProgress()
            if (!completedTask.isSuccessful) {
                showMessage(getString(string.login_error))
                updateUI(null)
            } else {
                try {
                    val account = completedTask.getResult(ApiException::class.java)
                    updateUI(account)
                } catch (e: ApiException) {
                    Timber.w("signInResult:failed code=" + e.statusCode)
                    updateUI(null)
                }
            }
        }
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        account?.let {
            Timber.w("signInResult:Login ID token=" + it.idToken)
            vm.login(it.idToken!!)
        }
    }

    private fun onLogin(it: UserInfo) {
        loginRequest = false
        sharedVm.userInfo(it)
    }

    private fun onFailure(failure: Failure?) {
        hideProgress()
        if (loginRequest) {
            loginRequest = false
            GoogleSignIn.getClient(this, gso!!).signOut()!!
            sharedVm.userInfo(null)
        }

        when (failure) {
            is Failure.NetworkConnection -> showMessage(failure.message)

            is Failure.FeatureFailure -> {
                if (failure.code == ERROR_LOGIN_CODE) {
                    showMessage(if (failure.message.isNullOrEmpty().not()) failure.message else getString(string.login_error))
                }
            }
            is Failure.ServerError -> {
                showMessage(failure.message)
            }
        }
    }

    override fun onBackPressed() {
        if (sharedVm.isPlayerExpand())
            sharedVm.collapsePlayer()
        else {
            super.onBackPressed()
        }
    }

    private var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent != null) {

                when (intent.action) {
                    ACTION_EPISODE -> {
                        val episode = intent.getParcelableExtra<Episode>(ACTION_EPISODE_GET)
                        sharedVm.notifyEpisode(Pair(UPDATE_VIEW, episode))
                    }
                    ACTION_PLAYER -> {
                        val action = intent.getIntExtra(ACTION_PLAYER_STATUS, MediaPlayerState.STATE_IDLE)
                        sharedVm.notifyPlayStatus(action)
                    }
                }
            }
        }
    }

    fun retrieveLatestEpisode() {
        sharedVm.isPlaying(true)
        vm.retrieveLatestEpisode()
    }

    private fun onLatestEpisode(episode: Episode) = sharedVm.notifyEpisode(Pair(SELECT_SINGLE_TRACK, episode))

    fun openEpisodeInfo(episode: Episode) {
        if (comFromDeepLink) {
            hideProgress()
            comFromDeepLink = false
            onLatestEpisode(episode)
        }

        addFragment(EpisodeFragment.newInstance(episode))
    }

    fun openPodcastInfo(p: Podcast) {
        if (comFromDeepLink) {
            hideProgress()
            comFromDeepLink = false
        }
        addFragment(PodcastFragment.newInstance(p))
    }

    fun openEpisodes(it: String) {
        addFragment(EpisodesFragment.getInstance(it))
    }

    fun preparePlaylist(
        playlist: ArrayList<Episode>,
        isLoadMore: Boolean = false
    ) {
        if (!isLoadMore)
            MediaPlayerServiceHelper.clearPlaylist(this)

        MediaPlayerServiceHelper.preparePlaylist(this, playlist)
    }

    fun prepareAndPlayPlaylist(playlist: ArrayList<Episode>, e: Episode) =
        MediaPlayerServiceHelper.prepareAndPlayPlaylist(this, playlist, e)
}
