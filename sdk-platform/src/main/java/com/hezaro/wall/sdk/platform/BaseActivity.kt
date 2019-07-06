package com.hezaro.wall.sdk.platform

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.Gravity
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.transition.Fade
import androidx.transition.Slide
import com.hezaro.wall.sdk.platform.ext.doAddToBackStack
import com.hezaro.wall.sdk.platform.ext.hide
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
        supportActionBar?.hide()
        // To prevent be add again when recreation happen
        if (supportFragmentManager.fragments.size == 0)
            addFragment(fragment())
    }

    fun progressbarMargin(margin: Int = -1) {
        val params = progressBar().layoutParams as CoordinatorLayout.LayoutParams
        if (params.bottomMargin != margin) {
            val animator =
                ValueAnimator.ofInt(params.bottomMargin, margin)
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

    fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun addFragment(fragment: BaseFragment) {
        when {
            (fragment.id() in 100..103) -> {
                fragment.enterTransition = Fade()
                fragment.exitTransition = Fade()
            }
            (fragment.id() in 104..105) -> {
                fragment.enterTransition = Slide(Gravity.BOTTOM)
                fragment.exitTransition = Slide(Gravity.TOP)
            }
            else -> {
                fragment.enterTransition = Slide(Gravity.END)
                fragment.exitTransition = Slide(Gravity.END)
            }
        }



        with(supportFragmentManager) {
            if (fragments.indexOf(findFragmentByTag(fragment.id().toString())) == -1) {
                beginTransaction()
                    .replace(fragmentContainer(), fragment, fragment.id().toString()).doAddToBackStack(fragments.size)
                    .commit()
            }
        }
    }
}