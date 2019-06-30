package com.hezaro.wall.feature.main

import android.view.View
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hezaro.wall.R
import com.hezaro.wall.feature.player.PlayerFragment
import org.junit.*
import org.junit.runner.*

@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    private val playerFragment: PlayerFragment by lazy {
        (mActivityTestRule.activity.supportFragmentManager.findFragmentById(R.id.playerFragment) as PlayerFragment?)!!
    }
    private val playerSheetBehavior: BottomSheetBehavior<View> by lazy {
        BottomSheetBehavior.from(playerFragment.view!!)
    }

    @Test
    fun startActivity() {
        onView(withId(R.id.progressBar)).isVisible()
    }

    @Test
    fun openPlayer() {
        onView(withId(R.id.progressBar)).isVisible()
        Thread.sleep(8000)
        onView(withId(R.id.progressBar)).isGone()
        Assert.assertTrue(
            (
                    playerSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN ||
                            playerSheetBehavior.peekHeight == 0)
        )

        onView(withId(R.id.episodeList))
            .perform(RecyclerViewActions.actionOnItemAtPosition<ViewHolder>(0, click()))
        Assert.assertFalse(
            (
                    playerSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN ||
                            playerSheetBehavior.peekHeight == 0)
        )
        Thread.sleep(1000)

        onView(withId(R.id.playerLayout)).perform(click())

        Assert.assertFalse(
            (
                    playerSheetBehavior.state == BottomSheetBehavior.STATE_HALF_EXPANDED &&
                            playerSheetBehavior.peekHeight != 0)
        )
    }
}
