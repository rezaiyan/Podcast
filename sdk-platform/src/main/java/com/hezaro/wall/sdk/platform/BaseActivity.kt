package com.hezaro.wall.sdk.platform

import android.os.Bundle
import android.view.Gravity
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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
    abstract fun toolbar(): Toolbar
    abstract fun progressBar(): ProgressBar
    abstract fun fragmentContainer(): Int
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addFragment(fragment())
    }

    fun showProgress() {
        progressBar().show()
    }

    fun hideProgress() {
        progressBar().hide()
    }

    fun addFragment(fragment: BaseFragment) {
        var gravity = Gravity.END
        if (fragment.tag() == "SearchFragment")
            gravity = Gravity.START
        fragment.enterTransition = Slide(gravity)
        fragment.exitTransition = Slide(gravity)

        if (fragment.tag() == "EpisodeFragment") {
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