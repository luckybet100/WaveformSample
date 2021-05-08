package dev.luckybet100.waveform.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import dev.luckybet100.waveform.R

class ZoomPressButton : FrameLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var clickListener: OnClickListener? = null

    override fun setOnClickListener(listener: OnClickListener?) {
        clickListener = listener
    }

    init {
        setBackgroundResource(R.drawable.bg_circle_button)
        setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                animation?.cancel()
                animate().scaleX(0.9f).scaleY(0.9f).apply {
                    duration = 50
                }.start()
            } else if (event.action == MotionEvent.ACTION_UP) {
                clickListener?.onClick(this)
                animation?.cancel()
                animate().scaleX(1f).scaleY(1f).apply {
                    duration = 50
                }.start()
            }
            true
        }
    }

}