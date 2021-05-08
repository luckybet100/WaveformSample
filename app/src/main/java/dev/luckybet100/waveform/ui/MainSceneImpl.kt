package dev.luckybet100.waveform.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import dev.luckybet100.waveform.utils.ColorAnimator
import dev.luckybet100.waveform.utils.dp
import dev.luckybet100.waveform.vm.MainViewModel

class MainSceneImpl(private val contact: MainScene.Contract) : MainScene {

    override fun bind(lifecycle: LifecycleOwner, vm: MainViewModel) {
        with(contact) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

            val changeTintBgList = listOf(
                startRecordIcon,
                stopRecordIcon,
                playIcon,
                resetIcon,
                pauseIcon
            )

            vm.backgroundColor.observe(lifecycle, Observer { color ->
                val oldColor =
                    (background.background as? ColorDrawable)?.color ?: Color.TRANSPARENT
                val colorAnimator = ColorAnimator(oldColor, color)
                background.animation?.cancel()
                background.animate().setUpdateListener {
                    val currentColor = colorAnimator.getColor(it.animatedFraction)
                    background.setBackgroundColor(currentColor)
                    //waveForm.applyBackgroundColor(currentColor)
                    window.statusBarColor = currentColor
                    window.navigationBarColor = currentColor
                    for (view in changeTintBgList) {
                        view.backgroundTintList = ColorStateList.valueOf(currentColor)
                    }
                }.start()
            })

            vm.recordingIcon.observe(lifecycle, Observer { icon ->
                startRecordIcon.animation?.cancel()
                stopRecordIcon.animation?.cancel()
                when (icon) {
                    MainViewModel.RecordingIcon.Start -> {
                        startRecordIcon.animate().scaleX(1f).scaleY(1f).start()
                        stopRecordIcon.animate().scaleX(0.6f).scaleY(0.6f).start()
                    }
                    MainViewModel.RecordingIcon.Record -> {
                        stopRecordIcon.animate().scaleX(1f).scaleY(1f).start()
                        startRecordIcon.animate().scaleX(0.6f).scaleY(0.6f).start()
                    }
                    else -> {
                        throw IllegalArgumentException("Error invalid icon type")
                    }
                }
            })

            vm.recordingText.observe(lifecycle, Observer {
                recordingText.text = it
            })

            vm.menu.observe(lifecycle, Observer { menu ->
                recordingMenu.animation?.cancel()
                playMenu.animation?.cancel()
                when (menu) {
                    MainViewModel.Menu.Record -> {
                        recordingMenu.animate()
                            .alpha(1f)
                            .translationY(-dp(recordingMenu.context, 4f))
                            .withEndAction {
                                recordingMenu.animate().translationY(dp(recordingMenu.context, 2f))
                                    .withEndAction {
                                        recordingMenu.animate().translationY(0f).start()
                                    }.start()
                            }.start()
                        playMenu.animate().alpha(0f).translationY(
                            dp(recordingMenu.context, 300f)
                        ).start()
                    }
                    MainViewModel.Menu.Play -> {
                        recordingMenu.animate().alpha(0f).translationY(
                            dp(recordingMenu.context, 300f)
                        ).start()
                        playMenu.animate()
                            .alpha(1f)
                            .translationY(-dp(recordingMenu.context, 4f))
                            .withEndAction {
                                playMenu.animate().translationY(dp(recordingMenu.context, 2f))
                                    .withEndAction {
                                        playMenu.animate().translationY(0f).start()
                                    }.start()
                            }.start()
                    }
                    else -> {
                        throw IllegalArgumentException("Error invalid menu type")
                    }
                }
            })

            vm.playingIcon.observe(lifecycle, Observer { icon ->
                playIcon.animation?.cancel()
                pauseIcon.animation?.cancel()
                when (icon) {
                    MainViewModel.PlayingIcon.Play -> {
                        playIcon.animate().scaleX(1f).scaleY(1f).translationX(0f).start()
                        pauseIcon.animate().scaleX(0.5f).scaleY(0.5f).start()
                    }
                    MainViewModel.PlayingIcon.Pause -> {
                        playIcon.animate().scaleX(0.2f).scaleY(0.2f)
                            .translationX(-dp(playIcon.context, 8f)).start()
                        pauseIcon.animate().scaleX(1f).scaleY(1f).start()
                    }
                    else -> {
                        throw IllegalArgumentException("Error invalid menu type")
                    }
                }
            })

            waveForm.bind(lifecycle, vm.waveFormViewModel)

            vm.requestPermissions.observe(lifecycle, Observer {
                if (it.isNotEmpty()) {
                    permissionManager.requestPermissions(it)
                }
            })

            vm.playingText.observe(lifecycle, Observer {
                playText.text = it
            })

            recordingButton.setOnClickListener {
                vm.notifyRecordingButtonClicked()
            }

            resetButton.setOnClickListener {
                vm.notifyResetButtonClicked()
            }

            playButton.setOnClickListener {
                vm.notifyPlayButtonClicked()
            }
        }

    }
}