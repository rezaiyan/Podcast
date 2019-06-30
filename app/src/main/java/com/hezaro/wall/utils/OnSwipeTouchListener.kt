package com.hezaro.wall.utils

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import java.util.Calendar

/**
 * Detects left and right swipes across a view.
 */

class OnSwipeTouchListener(
    private val panel: BottomSheetBehavior<View>,
    private val onStateChange: (Int) -> Unit,
    private val onSlideChange: (Float) -> Unit,
    private val function: () -> Unit
) : OnTouchListener {

    var dX: Float = 0.toFloat()
    var dY: Float = 0.toFloat()
    var startPointX: Float = 0.toFloat()
    var startPointY: Float = 0.toFloat()
    private val MAX_CLICK_DURATION = 100
    private var startClickTime: Long = 0
    private var view: View? = null

    init {
        panel.setBottomSheetCallback(object : BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) = onSlideChange(p1)

            override fun onStateChanged(p0: View, p1: Int) {
                onStateChange(p1)
                resetPosition()
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        this.view = view
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
                    (view.x >= view.width / 1.5) -> {
                        panel.peekHeight = 0
                        function.invoke()
                        setPosition((+view.width).toFloat())
                        resetPosition()
                    }
                    (-view.x >= view.width / 1.5) -> {
                        panel.peekHeight = 0
                        function.invoke()
                        setPosition((-view.width).toFloat())
                        resetPosition()
                    }
                    else -> {
                        val clickDuration = Calendar.getInstance().timeInMillis - startClickTime
                        if (clickDuration < MAX_CLICK_DURATION) {
                            panel.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                        resetPosition()
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                when {
                    Math.abs(event.rawY - startPointY) * 1.5 > Math.abs(event.rawX - startPointX) -> resetPosition()
                    panel.state == BottomSheetBehavior.STATE_COLLAPSED -> view.animate()
                        .x(event.rawX + dX)
                        .setDuration(0)
                        .start()
                    else -> resetPosition()
                }
            }
            else -> return false
        }
        return true
    }

    private fun resetPosition() {
        view?.animate()?.x(0F)?.setDuration(200)?.start()
    }

    private fun setPosition(position: Float) {
        view?.animate()
            ?.x(position)
            ?.setDuration(200)
            ?.start()
    }
}