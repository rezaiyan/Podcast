package com.hezaro.wall.feature.core.main

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.iid.FirebaseInstanceId
import com.hezaro.wall.R
import com.hezaro.wall.R.id
import com.hezaro.wall.R.string
import com.hezaro.wall.feature.explore.ExploreFragment
import com.hezaro.wall.sdk.platform.BaseActivity
import com.hezaro.wall.services.MediaPlayerService
import com.hezaro.wall.utils.CircleTransform
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_layout.progressBar
import kotlinx.android.synthetic.main.fragment_player.playerView
import kotlinx.android.synthetic.main.toolbar.toolbar
import timber.log.Timber

private const val RC_SIGN_IN = 991

class MainActivity : BaseActivity() {
    override fun fragment() = ExploreFragment()

    private lateinit var profile: ImageView
    private lateinit var mGoogleSignInClient: GoogleSignInClient

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
        Intent(this, MediaPlayerService::class.java).let {
            startService(it)
            bindService(it, serviceConnection, 0)
        }

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener { task ->
                val token = task.result?.token
                Timber.i("onNewIntent firebaseToken = $token")

            }
        val account = prepareGoogleSignIn()
        profile = findViewById(id.profile)
        profile.setOnClickListener {
            if (account == null) {
                signIn()
            } else
                Toast.makeText(this, "You already are login", Toast.LENGTH_SHORT).show()
        }
    }

    private fun prepareGoogleSignIn(): GoogleSignInAccount? {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(string.server_client_id))
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        return GoogleSignIn.getLastSignedInAccount(this)
    }

    override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(this)
        updateUI(account)
    }

    private fun signIn() {
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
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
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
            Timber.i("idToken== ${account.idToken}")
            Picasso.get().load(it.photoUrl).transform(CircleTransform()).into(profile)
        }
    }
}
