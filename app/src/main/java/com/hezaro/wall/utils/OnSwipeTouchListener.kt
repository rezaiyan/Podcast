package com.hezaro.wall.utils

import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.util.Calendar

/**
 * Detects left and right swipes across a view.
 */

class OnSwipeTouchListener(
    private val panel: BottomSheetBehavior<View>,
    private val function: () -> Unit
) : OnTouchListener {

    var dX: Float = 0.toFloat()
    var dY: Float = 0.toFloat()
    var startPointX: Float = 0.toFloat()
    var startPointY: Float = 0.toFloat()
    private val MAX_CLICK_DURATION = 100
    private var startClickTime: Long = 0

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                startClickTime = Calendar.getInstance().timeInMillis
                dX = view.x - event.rawX
                dY = view.y - event.rawY
                startPointX = event.rawX
                startPointY = event.rawY
            }

            MotionEvent.ACTION_UP -> {
                when {
                    (view.x >= view.width / 2) -> {
                        panel.peekHeight = 0
                        function.invoke()
                        setPosition(view, (+view.width).toFloat())
                        resetPosition(view)
                    }
                    (-view.x >= view.width / 2) -> {
                        panel.peekHeight = 0
                        function.invoke()
                        setPosition(view, (-view.width).toFloat())
                        resetPosition(view)
                    }
                    else -> {
                        val clickDuration = Calendar.getInstance().timeInMillis - startClickTime
                        if (clickDuration < MAX_CLICK_DURATION) {
                            panel.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                        resetPosition(view)
                    }
                }
//                panel.isTouchEnabled = true
            }

            MotionEvent.ACTION_MOVE -> {
                if (Math.abs(event.rawY - startPointY) * 2 > Math.abs(event.rawX - startPointX)) {
//                    panel.isTouchEnabled = true
                    resetPosition(view)
                } else {
//                    panel.isTouchEnabled = false
                    view.animate()
                        .x(event.rawX + dX)
                        .setDuration(0)
                        .start()
                }
            }
            else -> return false
        }
        return true
    }

    private fun resetPosition(view: View) {
        view.animate()
            .x(0F)
            .setDuration(200)
            .start()
    }

    private fun setPosition(view: View, position: Float) {
        view.animate()
            .x(position)
            .setDuration(200)
            .start()
    }
}