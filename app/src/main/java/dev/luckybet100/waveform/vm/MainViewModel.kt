package dev.luckybet100.waveform.vm

import androidx.lifecycle.LiveData
import dev.luckybet100.waveform.ui.views.WaveFormViewModel

interface MainViewModel {

    enum class RecordingIcon {
        Start,
        Record
    }

    enum class PlayingIcon {
        Play,
        Pause
    }


    enum class Menu {
        Record,
        Play
    }

    val waveFormViewModel: WaveFormViewModel

    val backgroundColor: LiveData<Int>

    val recordingIcon: LiveData<RecordingIcon>
    val recordingText: LiveData<String>

    val playingIcon: LiveData<PlayingIcon>
    val playingText: LiveData<String>

    val menu: LiveData<Menu>

    val requestPermissions: LiveData<List<String>>

    fun notifyRecordingButtonClicked()
    fun notifyResetButtonClicked()
    fun notifyPlayButtonClicked()

}