package dev.luckybet100.waveform.ui.views

import android.util.Log
import androidx.lifecycle.LiveData
import kotlin.math.*

interface WaveFormViewModel {

    interface State {


        object None : State {
            override fun getHeight(
                position: Int,
                minHeight: Float,
                maxHeight: Float,
                itemsOnScreen: Int
            ) = 0f

            override fun getOffset(itemsOnScreen: Int): Float = 0f
        }

        class Idle(
            val amplitudes: List<Int>,
            private val prevState: State,
            private val progress: Float
        ) : State {

            override fun getHeight(
                position: Int,
                minHeight: Float,
                maxHeight: Float,
                itemsOnScreen: Int
            ): Float {
                val amplitude = amplitudes.getOrNull(position) ?: 0
                val newHeight = max(minHeight, maxHeight * (amplitude / 32767f))
                val oldHeight = prevState.getHeight(position, minHeight, maxHeight, itemsOnScreen)
                return oldHeight + (newHeight - oldHeight) * progress
            }

            override fun getOffset(itemsOnScreen: Int): Float = 0f
        }

        class Recording(
            val amplitudes: List<Int>,
            val globalOffset: Float
        ) : State {

            private fun getPosition(value: Float): Int {
                if (ceil(value) == value)
                    return value.roundToInt() + 1
                return ceil(value).roundToInt()
            }

            override fun getHeight(
                position: Int,
                minHeight: Float,
                maxHeight: Float,
                itemsOnScreen: Int
            ): Float {
                val startIdx = max(0, getPosition(globalOffset) - itemsOnScreen)
                val amplitude = amplitudes.getOrNull(startIdx + position) ?: 0
                val height = maxHeight * (amplitude / 32768f)
                var progress = max(0f, min(5f, globalOffset - (startIdx + position))) / 5f
                progress = progress * progress * (3.0f - 2.0f * progress);
                return max(minHeight, height * progress)
            }

            override fun getOffset(itemsOnScreen: Int): Float = if (globalOffset < itemsOnScreen) {
                0f
            } else {
                1f - (globalOffset - floor(globalOffset))
            }
        }

        fun getHeight(position: Int, minHeight: Float, maxHeight: Float, itemsOnScreen: Int): Float
        fun getOffset(itemsOnScreen: Int): Float

        class Result(
            val amplitudes: List<Int>,
            val prevState: State,
            val progress: Float,
            val playProgress: Float,
            val fadeProgress: Float
        ) : State {

            override fun getHeight(
                position: Int,
                minHeight: Float,
                maxHeight: Float,
                itemsOnScreen: Int
            ): Float {
                val scale: Float = amplitudes.size.toFloat() / itemsOnScreen
                val left = floor(position * scale).roundToInt()
                var right = ceil((position + 1) * scale).roundToInt()
                if (right == left)
                    ++right
                var amplitude = 0
                for (index in left until right)
                    amplitude += amplitudes.getOrNull(index) ?: 0
                amplitude /= (right - left)
                val newHeight = max(minHeight, maxHeight * (amplitude / 32768f))
                val oldHeight = prevState.getHeight(position, minHeight, maxHeight, itemsOnScreen)
                return oldHeight + (newHeight - oldHeight) * progress
            }

            override fun getOffset(itemsOnScreen: Int): Float = 0f

            fun getAlpha(index: Int, itemsOnScreen: Int): Int {
                val position = playProgress * itemsOnScreen
                val requiredAlpha = (150 + (255 - 150) * max(
                    0f,
                    min(1f, (position - index))
                )).toInt()
                val animatedAlpha = (255 + (150 - 255) * fadeProgress)
                return max(requiredAlpha, animatedAlpha.toInt())
            }
        }

    }


    val waveFormState: LiveData<State>

}