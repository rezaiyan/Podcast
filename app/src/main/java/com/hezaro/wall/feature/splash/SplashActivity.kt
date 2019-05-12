package com.hezaro.wall.feature.splash

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.hezaro.wall.R
import com.hezaro.wall.data.model.Version
import com.hezaro.wall.feature.main.MainActivity
import com.hezaro.wall.sdk.base.exception.Failure
import org.koin.android.ext.android.inject

class SplashActivity : AppCompatActivity() {

    private val vm: SplashViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        with(vm) {
            observe(version, ::onVersion)
            failure(failure, ::onFailure)
            version()
        }
    }

    private fun onVersion(version: Version) {
        if (version.force_update)
            forceUpdateDialog()
        else startActivity(
            Intent(
                this,
                MainActivity::class.java
            ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun forceUpdateDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_force_update)
        dialog.findViewById<Button>(R.id.exit)
            .setOnClickListener { System.exit(0) }
        dialog.findViewById<Button>(R.id.update).setOnClickListener {
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("http://hezaro.com")
                startActivity(this)
            }
        }
        dialog.show()
    }

    private fun onFailure(failure: Failure) {
        startActivity(Intent(this, MainActivity::class.java))
    }
}