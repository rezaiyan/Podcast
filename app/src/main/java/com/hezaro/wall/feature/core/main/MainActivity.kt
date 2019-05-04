package com.hezaro.wall.feature.core.main

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.iid.FirebaseInstanceId
import com.hezaro.wall.R
import com.hezaro.wall.R.string
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.UserInfo
import com.hezaro.wall.data.model.Version
import com.hezaro.wall.feature.core.player.PlayerFragment
import com.hezaro.wall.feature.episode.EpisodeFragment
import com.hezaro.wall.feature.explore.ExploreFragment
import com.hezaro.wall.feature.profile.ProfileFragment
import com.hezaro.wall.feature.search.SearchFragment
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.platform.BaseActivity
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.sdk.platform.ext.load
import com.hezaro.wall.sdk.platform.player.download.DownloadTracker
import com.hezaro.wall.sdk.platform.player.download.DownloadTracker.Listener
import com.hezaro.wall.sdk.platform.player.download.PlayerDownloadHelper
import com.hezaro.wall.services.MediaPlayerService
import com.hezaro.wall.utils.CircleTransform
import com.hezaro.wall.utils.RC_SIGN_IN
import kotlinx.android.synthetic.main.activity_layout.progressBar
import kotlinx.android.synthetic.main.fragment_player.playerView
import kotlinx.android.synthetic.main.toolbar.profile
import kotlinx.android.synthetic.main.toolbar.toolbar
import org.koin.android.ext.android.inject
import timber.log.Timber

class MainActivity : BaseActivity(), Listener {
    override fun onDownloadsChanged() {
    }

    override fun fragment() = ExploreFragment()

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val vm: MainViewModel by inject()
    override fun layoutId() = R.layout.activity_layout
    override fun toolbar(): Toolbar = toolbar
    override fun progressBar(): ProgressBar = progressBar
    override fun fragmentContainer() = R.id.fragmentContainer
    private val playerFragment: PlayerFragment by lazy { (supportFragmentManager?.findFragmentById(R.id.playerFragment) as PlayerFragment?)!! }
    private val downloader: DownloadTracker by lazy {
        PlayerDownloadHelper(
            applicationContext
        ).dlTracker!!
    }

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
        with(vm) {
            observe(login, ::onLogin)
            failure(failure, ::onFailure)
            observe(version, ::onVersion)
            version()
        }

        startService(Intent(this, MediaPlayerService::class.java))

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener { task ->
                val token = task.result?.token
                Timber.i("onNewIntent firebaseToken = $token")

            }
        prepareGoogleSignIn()
    }

    fun startDownload(episode: Episode) {
        downloader.toggleDownload(this, episode.title, Uri.parse(episode.source), ".mp3")
    }

    private fun bindService() {
        bindService(Intent(this, MediaPlayerService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun search() {
        playerFragment.collapse(); addFragment(SearchFragment())
    }

    fun episode() {
        val fragment = EpisodeFragment()
        playerFragment.collapse(); addFragment(fragment)
    }

    fun profile() {
        if (googleSignInAccount() == null) {
            signIn()
        } else {
            playerFragment.collapse();addFragment(ProfileFragment())
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
        downloader.removeListener(this)
        unbindService(serviceConnection)
    }

    override fun onStart() {
        super.onStart()
        downloader.addListener(this)
        bindService()
        val account = googleSignInAccount()
        updateUI(account)
    }

    private fun googleSignInAccount() = GoogleSignIn.getLastSignedInAccount(this)

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
            // The ApiException status code indicates the detailed failure reason.
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
        profile.load(it.avatar, CircleTransform())
    }

    private fun onVersion(it: Version) {
        if (!it.force_update)
            forceUpdateDialog()
    }

    private fun onFailure(failure: Failure) {
        mGoogleSignInClient.signOut()
    }

    override fun onBackPressed() {
        if (playerFragment.isExpand())
            playerFragment.collapse()
        else {
            (supportFragmentManager.findFragmentById(fragmentContainer()) as BaseFragment).onBackPressed()
            super.onBackPressed()
        }
    }
}
