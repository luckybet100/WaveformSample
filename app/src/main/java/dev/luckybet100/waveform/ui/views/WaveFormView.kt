package dev.luckybet100.waveform.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import dev.luckybet100.waveform.R
import dev.luckybet100.waveform.utils.dp
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt


class WaveFormView : View {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var state: WaveFormViewModel.State = WaveFormViewModel.State.None

    private val lineWidth = dp(context, 4f)
    private val cornerRadius = dp(context, 100f)
    private val maxHeight = dp(context, 300f)

    private val paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.colorWaveform)
        style = Paint.Style.FILL
        flags = Paint.ANTI_ALIAS_FLAG
    }

    fun bind(lifecycle: LifecycleOwner, vm: WaveFormViewModel) {
        vm.waveFormState.observe(lifecycle, Observer {
            state = it
            invalidate()
        })
    }

    override fun onDraw(canvas: Canvas) {
        val itemWidth = lineWidth * 2
        val itemsOnScreen = ceil(width / itemWidth).roundToInt() + 1
        val offset = state.getOffset(itemsOnScreen) * lineWidth * 2
        val state = state
        for (index in 0 until itemsOnScreen) {
            val lineLeft = offset + itemWidth * index
            val lineHeight = state.getHeight(
                index,
                lineWidth / 2f,
                min(height.toFloat(), maxHeight) / 2f,
                itemsOnScreen
            )
            if (state is WaveFormViewModel.State.Result) {
                paint.alpha = state.getAlpha(index, itemsOnScreen)
            } else {
                paint.alpha = 255
            }
            canvas.drawRoundRect(
                lineLeft,
                height / 2f + lineHeight,
                lineLeft + lineWidth,
                height / 2f - lineHeight,
                cornerRadius,
                cornerRadius,
                paint
            )
        }
    }

}