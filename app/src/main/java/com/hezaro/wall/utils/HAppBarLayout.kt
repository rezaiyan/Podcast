package com.hezaro.wall.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import com.google.android.material.appbar.AppBarLayout
import com.hezaro.wall.R
import com.hezaro.wall.sdk.platform.ext.toRange

private const val NONE = 0
private const val TOP_LEFT = 1
private const val TOP_RIGHT = 2
private const val BOTTOM_LEFT = 4
private const val BOTTOM_RIGHT = 8
private const val ALL = 15

class HAppBar : AppBarLayout {

    private var mPath: Path? = null
    private var radius: Float = 50.toFloat()
    private var cornerPosition = NONE
    val radii = FloatArray(8)

    constructor(context: Context) : super(context) {
        clipCorner(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        clipCorner(context, attrs)
    }

    private fun clipCorner(context: Context, attrs: AttributeSet?) {
        attrs?.let {
            val attributes = context.obtainStyledAttributes(attrs, R.styleable.HAppBar)
            cornerPosition = attributes.getInt(R.styleable.HAppBar_corner_position, NONE)
            attributes.recycle()
        }
    }

    fun updateRadius(value: Float) {
        this.radius = value.toRange(0..1, 50..0)
        invalidate()
    }

    override fun draw(canvas: Canvas?) {
        clipCorner(width, height)
        canvas?.clipPath(mPath)
        canvas?.save()
        super.draw(canvas)
        canvas?.restore()
    }

    private fun clipCorner(w: Int, h: Int) {
        val r = RectF(0f, 0f, w.toFloat(), h.toFloat())
        mPath = Path()


        if (cornerPosition == NONE) {
        }
        if (cornerPosition == TOP_LEFT) {
            radii[0] = radius
            radii[1] = radius
        }
        if (cornerPosition == TOP_RIGHT) {
            radii[2] = radius
            radii[3] = radius
        }
        if (cornerPosition == BOTTOM_LEFT) {
            radii[4] = radius
            radii[5] = radius
        }
        if (cornerPosition == BOTTOM_RIGHT) {
            radii[6] = radius
            radii[7] = radius
        }

        if (cornerPosition == TOP_LEFT + TOP_RIGHT) {
            radii[0] = radius
            radii[1] = radius
            radii[2] = radius
            radii[3] = radius
        }
        if (cornerPosition == BOTTOM_LEFT + BOTTOM_RIGHT) {
            radii[4] = radius
            radii[5] = radius
            radii[6] = radius
            radii[7] = radius
        }

        if (cornerPosition == ALL) {
            radii[0] = radius
            radii[1] = radius
            radii[2] = radius
            radii[3] = radius
            radii[4] = radius
            radii[5] = radius
            radii[6] = radius
            radii[7] = radius
        }


        mPath?.addRoundRect(r, radii, Path.Direction.CW)
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline?) {
                outline?.setConvexPath(mPath)
            }
        }
        mPath?.close()
    }
}