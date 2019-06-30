package com.hezaro.wall.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import com.google.android.material.bottomappbar.BottomAppBar
import com.hezaro.wall.R

private const val NONE = 0
private const val TOP_LEFT = 1
private const val TOP_RIGHT = 2
private const val BOTTOM_LEFT = 4
private const val BOTTOM_RIGHT = 8
private const val ALL = 15

class HBottomAppBar : BottomAppBar {

    private var mPath: Path? = null
    private var radius: Float = 50.toFloat()
    private var cornerPosition = NONE

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        attrs?.let {
            val attributes = context.obtainStyledAttributes(attrs, R.styleable.HAppBar)
            cornerPosition = attributes.getInt(R.styleable.HAppBar_corner_position, NONE)
            attributes.recycle()
        }
    }

    override fun draw(canvas: Canvas) {
        canvas.save()
        canvas.clipPath(mPath)
        super.draw(canvas)
        canvas.restore()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val r = RectF(0f, 0f, w.toFloat(), h.toFloat())
        mPath = Path()
        val radii = FloatArray(8)

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