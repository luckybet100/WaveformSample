package dev.luckybet100.waveform.utils

import android.graphics.Color

class ColorAnimator(private val oldColor: Int, private val newColor: Int) {

    fun getColor(animatedFraction: Float): Int {
        val oldRed = Color.red(oldColor)
        val oldGreen = Color.green(oldColor)
        val oldBlue = Color.blue(oldColor)
        val oldAlpha = Color.alpha(oldColor)
        val newRed = Color.red(newColor)
        val newGreen = Color.green(newColor)
        val newBlue = Color.blue(newColor)
        val newAlpha = Color.alpha(newColor)
        val red = oldRed + animatedFraction * (newRed - oldRed)
        val green = oldGreen + animatedFraction * (newGreen - oldGreen)
        val blue = oldBlue + animatedFraction * (newBlue - oldBlue)
        val alpha = oldAlpha + animatedFraction * (newAlpha - oldAlpha)
        return Color.argb(alpha.toInt(), red.toInt(), green.toInt(), blue.toInt())
    }

}
