package com.hezaro.wall.feature.core.main

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
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
import com.hezaro.wall.data.model.UserInfo
import com.hezaro.wall.feature.core.player.PlayerFragment
import com.hezaro.wall.feature.explore.ExploreFragment
import com.hezaro.wall.feature.profile.ProfileFragment
import com.hezaro.wall.feature.search.SearchFragment
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.platform.BaseActivity
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.services.MediaPlayerService
import com.hezaro.wall.services.RC_SIGN_IN
import com.hezaro.wall.utils.CircleTransform
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_layout.progressBar
import kotlinx.android.synthetic.main.fragment_player.playerView
import kotlinx.android.synthetic.main.toolbar.profile
import kotlinx.android.synthetic.main.toolbar.toolbar
import org.koin.android.ext.android.inject
import timber.log.Timber

class MainActivity : BaseActivity() {
    override fun fragment() = ExploreFragment()

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val vm: MainViewModel by inject()
    override fun layoutId() = R.layout.activity_layout
    override fun toolbar(): Toolbar = toolbar
    override fun progressBar(): ProgressBar = progressBar
    override fun fragmentContainer() = R.id.fragmentContainer
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            playerView.player = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val playerService = (service as MediaPlayerService.ServiceBinder).service
            playerService.serviceConnected(this@MainActivity)
            playerView.player = playerService.player
        }
    }

    override fun onDestroy() {
        unbindService(serviceConnection)
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId())

        with(vm) {
            observe(userInfo, ::onSuccess)
            failure(failure, ::onFailure)
        }


        Intent(this, MediaPlayerService::class.java).let {
            startService(it)
            bindService(it, serviceConnection, 0)
        }

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener { task ->
                val token = task.result?.token
                Timber.i("onNewIntent firebaseToken = $token")

            }
        prepareGoogleSignIn()

    }

    private val playerFragment: PlayerFragment by lazy { (supportFragmentManager?.findFragmentById(R.id.playerFragment) as PlayerFragment?)!! }

    fun search() {
        playerFragment.collapse(); addFragment(SearchFragment())
    }

    fun profile() {
        //            if (googleSignInAccount() == null) {
//                signIn()
//            } else {
        playerFragment.collapse();addFragment(ProfileFragment())
//            }
    }

    private fun prepareGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(string.server_client_id))
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    override fun onStart() {
        super.onStart()
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

    private fun onSuccess(userInfo: UserInfo) {
        Picasso.get().load(GoogleSignIn.getLastSignedInAccount(this)!!.photoUrl).transform(CircleTransform())
            .into(profile)
    }

    private fun onFailure(failure: Failure) {
        mGoogleSignInClient.signOut()
    }

    override fun onBackPressed() {
        val playerFragment = (supportFragmentManager?.findFragmentById(R.id.playerFragment) as PlayerFragment?)!!
        if (playerFragment.isExpand())
            playerFragment.collapse()
        else {
            (supportFragmentManager.findFragmentById(fragmentContainer()) as BaseFragment).onBackPressed()
            super.onBackPressed()
        }
    }
}
