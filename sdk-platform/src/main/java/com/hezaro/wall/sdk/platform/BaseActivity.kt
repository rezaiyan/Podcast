package com.hezaro.wall.sdk.platform

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.Gravity
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.transition.Fade
import androidx.transition.Slide
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
    abstract fun fragment(): BaseFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addFragment(fragment())
    }

    fun progressbarMargin(i: Int = -1) {
        val params = progressBar().layoutParams as CoordinatorLayout.LayoutParams
        if (params.bottomMargin == 0 || i >= 0) {
            val animator =
                ValueAnimator.ofInt(
                    params.bottomMargin,
                    resources.getDimension(if (i == 0) R.dimen.progress_margin_8 else R.dimen.progress_margin).toInt()
                )
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

    fun addFragment(fragment: BaseFragment) {
        val gravity = Gravity.END
        if (fragment.tag() == "SearchFragment" || fragment.tag() == "ExploreFragment") {
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
                inTransaction { replace(fragmentContainer(), fragment, fragment.tag()) }
            }
        }
    }
}