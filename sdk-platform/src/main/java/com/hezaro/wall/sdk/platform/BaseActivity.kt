package com.hezaro.wall.sdk.platform

import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.transition.Fade
import androidx.transition.Slide
import com.google.android.material.button.MaterialButton
import com.hezaro.wall.sdk.platform.ext.hide
import com.hezaro.wall.sdk.platform.ext.inTransaction
import com.hezaro.wall.sdk.platform.ext.show

/**
 * Base Activity class with helper methods for handling fragment transactions and back button
 * events.
 *
 */
abstract class BaseActivity : AppCompatActivity() {

    abstract fun layoutId(): Int
    abstract fun progressBar(): ProgressBar
    abstract fun fragmentContainer(): Int
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addFragment(fragment())
    }

    fun progressbarMargin() {
        val params = progressBar().layoutParams as CoordinatorLayout.LayoutParams
        if (params.bottomMargin == 0) {
            val animator =
                ValueAnimator.ofInt(params.bottomMargin, resources.getDimension(R.dimen.progress_margin).toInt())
            animator.addUpdateListener { valueAnimator ->
                params.bottomMargin = valueAnimator.animatedValue as Int
                progressBar().requestLayout()
            }
            animator.duration = 100
            animator.start()
        }
    }

    fun showProgress() {
        progressBar().show()
    }

    fun hideProgress() {
        progressBar().hide()
    }

    fun forceUpdateDialog() {
        val dialog = Dialog(this)
        dialog.setTitle("ورژن جدید اپلیکیشن در دسترس می باشد")
        dialog.setContentView(R.layout.dialog_force_update)
        dialog.findViewById<MaterialButton>(R.id.exit).setOnClickListener { System.exit(0) }
        dialog.findViewById<MaterialButton>(R.id.update).setOnClickListener {
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("http://wall.hezaro.com")
                startActivity(this)
            }
        }
    }

    fun addFragment(fragment: BaseFragment) {
        var gravity = Gravity.END
        if (fragment.tag() == "SearchFragment") {
            fragment.enterTransition = Fade()
            fragment.exitTransition = Fade()
        } else {
            fragment.enterTransition = Slide(gravity)
            fragment.exitTransition = Slide(gravity)
        }

        if (fragment.tag() == "EpisodeFragment" || fragment.tag() == "PodcastFragment") {
            fragment.enterTransition = Slide(Gravity.BOTTOM)
            fragment.exitTransition = Slide(Gravity.TOP)
        }

        with(supportFragmentManager) {
            if (fragments.indexOf(findFragmentByTag(fragment.tag())) == -1) {
                inTransaction { add(fragmentContainer(), fragment, fragment.tag()) }
            }
        }
    }

    abstract fun fragment(): BaseFragment
}