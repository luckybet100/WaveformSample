package dev.luckybet100.waveform.vm

import android.Manifest
import android.animation.ValueAnimator
import android.app.Application
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import dev.luckybet100.waveform.R
import dev.luckybet100.waveform.ui.views.WaveFormViewModel
import dev.luckybet100.waveform.utils.MediaHelper
import dev.luckybet100.waveform.utils.PermissionChecker
import java.io.File

class MainViewModelImpl(
    application: Application
) : MainViewModel,
    AndroidViewModel(application),
    WaveFormViewModel,
    LifecycleObserver {

    companion object {
        private val PERMISSIONS = listOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    private enum class State {
        Start,
        PreparingRecorder,
        Recording,
        Result,
        Playing
    }

    private val permissionManager: PermissionChecker = PermissionChecker()
    private val mediaHelper =
        MediaHelper(
            this::recorderPrepared,
            this::playFinished,
            this::recorderAmplitude,
            this::playerProgress
        )

    private val audioFile =
        File.createTempFile("WaveformSampleRecord", ".m4a", getApplication<Application>().cacheDir)

    override val backgroundColor = MutableLiveData<Int>()

    override val recordingIcon = MutableLiveData<MainViewModel.RecordingIcon>()
    override val recordingText = MutableLiveData<String>()

    override val playingIcon = MutableLiveData<MainViewModel.PlayingIcon>()
    override val playingText = MutableLiveData<String>()
    override val menu = MutableLiveData<MainViewModel.Menu>()

    private val state = MutableLiveData<State>()

    override val requestPermissions = MutableLiveData<List<String>>()

    override val waveFormViewModel = this
    override val waveFormState = MutableLiveData<WaveFormViewModel.State>()

    init {
        setStartState()
    }

    private var animation: ValueAnimator? = null

    private fun setupResultFromRecord() {
        runAnimation {
            val oldState = waveFormState.value
            val amplitudes = (oldState as? WaveFormViewModel.State.Idle)?.amplitudes
                ?: (oldState as? WaveFormViewModel.State.Recording)?.amplitudes
            if (amplitudes != null && oldState != null) {
                animation?.cancel()
                animation = ValueAnimator.ofFloat(0f, 1f).apply {
                    addUpdateListener {
                        waveFormState.value = WaveFormViewModel.State.Result(
                            amplitudes,
                            oldState,
                            it.animatedFraction,
                            1f,
                            0f
                        )
                    }
                    duration = 300
                    start()
                }
            }
        }
    }

    override fun notifyRecordingButtonClicked() {
        when (state.value) {
            State.Start -> {
                if (permissionManager.checkPermissions(getApplication(), PERMISSIONS)) {
                    state.value = State.PreparingRecorder
                    mediaHelper.prepareRecorder(audioFile.absolutePath)
                } else {
                    requestPermissions.value = PERMISSIONS
                }
            }
            State.Recording -> {
                setupResultFromRecord()
                mediaHelper.stopRecorder()
                setResultState()
            }
        }
    }

    override fun notifyResetButtonClicked() {
        when (state.value) {
            State.Result -> setStartState()
            State.Playing -> {
                mediaHelper.stopPlayer()
                setStartState()
            }
        }
    }

    override fun notifyPlayButtonClicked() {
        when (state.value) {
            State.Result -> {
                runAnimation {
                    val oldState = waveFormState.value
                    if (oldState is WaveFormViewModel.State.Result) {
                        mediaHelper.preparePlayer(audioFile.absolutePath)
                        mediaHelper.startPlayer()
                        setPlayingState()
                        animation?.cancel()
                        ValueAnimator.ofFloat(0f, 1f).apply {
                            addUpdateListener {
                                val nowState = waveFormState.value ?: return@addUpdateListener
                                if (nowState !is WaveFormViewModel.State.Result)
                                    return@addUpdateListener
                                waveFormState.value = WaveFormViewModel.State.Result(
                                    oldState.amplitudes,
                                    oldState.prevState,
                                    1f,
                                    nowState.playProgress,
                                    it.animatedFraction
                                )
                            }
                            duration = 300
                            start()
                        }
                    }
                }
            }
            State.Playing -> {
                runAnimation {
                    val oldState = waveFormState.value
                    if (oldState is WaveFormViewModel.State.Result) {
                        animation?.cancel()
                        animation = ValueAnimator.ofFloat(0f, 1f).apply {
                            addUpdateListener {
                                waveFormState.value = WaveFormViewModel.State.Result(
                                    oldState.amplitudes,
                                    oldState.prevState,
                                    1f,
                                    if (it.animatedFraction == 1f) 1f else oldState.playProgress,
                                    1f - it.animatedFraction
                                )
                            }
                            duration = 300
                            start()
                        }
                        mediaHelper.stopPlayer()
                        setResultState()
                    }
                }
            }
        }
    }

    private fun recorderAmplitude(value: Int, offset: Float) {
        val oldState = waveFormState.value ?: WaveFormViewModel.State.None
        if (oldState is WaveFormViewModel.State.Recording || oldState is WaveFormViewModel.State.Idle) {
            animation?.cancel()
            val oldAmplitudes =
                (oldState as? WaveFormViewModel.State.Recording)?.amplitudes ?: emptyList()
            waveFormState.value = WaveFormViewModel.State.Recording(
                oldAmplitudes + if (value >= 0) listOf(value) else emptyList(),
                offset
            )
        }
    }

    private fun runAnimation(newAnimator: () -> Unit) {
        val animation = animation
        if (animation?.isRunning == true) {
            animation.doOnEnd {
                newAnimator.invoke()
            }
        } else {
            newAnimator.invoke()
        }
    }

    private fun playerProgress(progress: Float) {
        val currentProgress: Float
        if (state.value == State.Playing) {
            currentProgress = progress
        } else {
            currentProgress = 1f
        }
        val oldState = waveFormState.value ?: WaveFormViewModel.State.None
        if (oldState is WaveFormViewModel.State.Result) {
            waveFormState.value = WaveFormViewModel.State.Result(
                oldState.amplitudes,
                oldState.prevState,
                oldState.progress,
                currentProgress,
                oldState.fadeProgress
            )
        }
    }

    private fun recorderPrepared() {
        if (state.value == State.PreparingRecorder) {
            mediaHelper.startRecorder()
            setRecordingState()
        } else {
            throw Exception()
        }
    }

    private fun playFinished() {
        if (state.value == State.Playing)
            setResultState()
    }

    private fun setStartState() {
        backgroundColor.value = ContextCompat.getColor(getApplication(), R.color.colorStart)
        recordingIcon.value = MainViewModel.RecordingIcon.Start
        recordingText.value = getApplication<Application>().getString(R.string.start_record)
        if (menu.value != null && menu.value != MainViewModel.Menu.Record)
            menu.value = MainViewModel.Menu.Record
        state.value = State.Start
        runAnimation {
            val oldState = waveFormState.value ?: WaveFormViewModel.State.None
            animation?.cancel()
            animation = ValueAnimator.ofFloat(0f, 1f).apply {
                addUpdateListener {
                    waveFormState.value = WaveFormViewModel.State.Idle(
                        emptyList(),
                        oldState,
                        it.animatedFraction
                    )
                }
                duration = 300
                start()
            }
        }
    }

    private fun setRecordingState() {
        backgroundColor.value = ContextCompat.getColor(getApplication(), R.color.colorRecording)
        recordingIcon.value = MainViewModel.RecordingIcon.Record
        recordingText.value = getApplication<Application>().getString(R.string.finish_record)
        if (menu.value != null && menu.value != MainViewModel.Menu.Record)
            menu.value = MainViewModel.Menu.Record
        state.value = State.Recording
    }

    private fun setResultState() {
        backgroundColor.value = ContextCompat.getColor(getApplication(), R.color.colorResult)
        if (menu.value != MainViewModel.Menu.Play)
            menu.value = MainViewModel.Menu.Play
        state.value = State.Result
        playingIcon.value = MainViewModel.PlayingIcon.Play
        playingText.value = getApplication<Application>().getString(R.string.play_record)
    }

    private fun setPlayingState() {
        state.value = State.Playing
        playingIcon.value = MainViewModel.PlayingIcon.Pause
        playingText.value = getApplication<Application>().getString(R.string.pause_record)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun setStoppedState() {
        requestPermissions.value = emptyList()
        when (state.value) {
            State.Recording -> {
                setupResultFromRecord()
                mediaHelper.stopRecorder()
                setResultState()
            }
            State.Playing -> {
                setResultState()
                mediaHelper.stopPlayer()
                playerProgress(1f)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaHelper.release()
    }
}