package dev.luckybet100.waveform.ui

import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import dev.luckybet100.waveform.R
import dev.luckybet100.waveform.ui.views.WaveFormView
import dev.luckybet100.waveform.utils.PermissionManager
import dev.luckybet100.waveform.vm.MainViewModel

interface MainScene {

    companion object {
        fun create(contact: Contract): MainScene = MainSceneImpl(contact)
    }

    fun bind(lifecycle: LifecycleOwner, vm: MainViewModel)

    interface Contract {

        companion object {
            fun create(window: Window, permissionManager: PermissionManager): Contract =
                GenericContract(window.decorView, window, permissionManager)
        }

        val background: View
        val recordingButton: View
        val window: Window

        val recordingMenu: View
        val startRecordIcon: View
        val stopRecordIcon: View
        val recordingText: TextView

        val playMenu: View
        val resetButton: View
        val resetIcon: View
        val playButton: View
        val playIcon: View
        val pauseIcon: View
        val playText: TextView

        val waveForm: WaveFormView

        val permissionManager: PermissionManager
    }

    private class GenericContract(
        view: View,
        override val window: Window,
        override val permissionManager: PermissionManager
    ) : Contract {

        override val background: View = view.findViewById(R.id.background)

        override val recordingMenu: View = view.findViewById(R.id.recordingMenu)
        override val recordingButton: View = view.findViewById(R.id.recordingButton)
        override val startRecordIcon: View = view.findViewById(R.id.startRecordIcon)
        override val stopRecordIcon: View = view.findViewById(R.id.stopRecordIcon)
        override val recordingText: TextView = view.findViewById(R.id.recordingText)

        override val playMenu: View = view.findViewById(R.id.playMenu)
        override val resetButton: View = view.findViewById(R.id.resetButton)
        override val resetIcon: View = view.findViewById(R.id.resetIcon)
        override val playButton: View = view.findViewById(R.id.playButton)
        override val playIcon: View = view.findViewById(R.id.playIcon)
        override val pauseIcon: View = view.findViewById(R.id.pauseIcon)
        override val playText: TextView = view.findViewById(R.id.playText)

        override val waveForm: WaveFormView = view.findViewById(R.id.waveform)
    }


}