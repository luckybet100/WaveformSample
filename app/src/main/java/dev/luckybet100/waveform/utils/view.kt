package dev.luckybet100.waveform.utils

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue


fun dp(ctx: Context, value: Float): Float {
    return dp(ctx.resources, value)
}

fun dp(resources: Resources, value: Float): Float {
    return dp(resources.displayMetrics, value)
}

fun dp(dm: DisplayMetrics, value: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, dm)
}