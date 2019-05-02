package com.hezaro.wall.feature.core.player

import android.app.Dialog
import android.content.Context
import android.widget.NumberPicker
import com.hezaro.wall.R

class SpeedPicker(context: Context, closeAction: (Float) -> Unit) {
    val dialog = Dialog(context)
    val numberPicker: NumberPicker by lazy { dialog.findViewById<NumberPicker>(R.id.numberPicker) }
    val numbers = arrayOfNulls<String>(26)

    fun show(defaultValue: Float) {
        numberPicker.value = (defaultValue * 10).toInt() - 4
        dialog.show()
    }

    init {

        dialog.setContentView(R.layout.dialog_speed_chooser)

        for (x in 5..30) {
            numbers[x - 5] = "${(x.toFloat() / 10)}x"
        }

        numberPicker.minValue = 1
        numberPicker.maxValue = numbers.size
        numberPicker.wrapSelectorWheel = false
        numberPicker.displayedValues = numbers
        numberPicker.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        numberPicker.setOnValueChangedListener { _, oldValue, newVal ->
            val speed = numbers[newVal - 1]!!.substring(0, 3)
            closeAction(speed.toFloat())
        }
    }
}