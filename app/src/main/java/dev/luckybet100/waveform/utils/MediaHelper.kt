package dev.luckybet100.waveform.utils

import android.media.MediaPlayer
import android.media.MediaRecorder
import kotlinx.coroutines.*
import kotlin.math.abs
import kotlin.math.round

class MediaHelper(
    private val recorderPreparedCallback: () -> Unit,
    private val playFinishedCallback: () -> Unit,
    private val amplitudeCallback: (Int, Float) -> Unit,
    private val playProgressListener: (Float) -> Unit
) {

    companion object {
        private const val BIT_DEPTH = 16
        private const val SAMPLE_RATE = 44100
        private const val BIT_RATE = SAMPLE_RATE * BIT_DEPTH
        const val TIME_TO_TRACK = 64L
    }

    private val mediaContext = newSingleThreadContext("media_thread")
    private val scope = MainScope()

    private var recorderStarted = false
    private val mediaRecorder by lazy { MediaRecorder() }
    private val mediaPlayer by lazy { MediaPlayer() }
    private var recordingProgress: Deferred<Unit>? = null


    fun prepareRecorder(audioFile: String) = scope.launch(mediaContext) {
        mediaRecorder.reset()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder.setAudioEncodingBitRate(BIT_RATE)
        mediaRecorder.setAudioSamplingRate(SAMPLE_RATE)
        mediaRecorder.setOutputFile(audioFile)
        mediaRecorder.prepare()
        scope.launch(Dispatchers.Main) {
            recorderPreparedCallback.invoke()
        }
    }

    fun startRecorder() = scope.launch(mediaContext) {
        mediaRecorder.start()
        while (mediaRecorder.maxAmplitude == 0) // this need because if u stop just after start, u get exception. this is needed to track real start
            Thread.sleep(1) // i need to block media thread to avoid stopping before real start
        recordingProgress?.cancel()
        recordingProgress = scope.async(mediaContext) {
            val startTime = System.currentTimeMillis()
            var index = 0
            while (recorderStarted) {
                val timeUpdate = startTime + index * TIME_TO_TRACK
                val now = System.currentTimeMillis()
                var offset = (now - startTime).toFloat() / TIME_TO_TRACK
                if (now >= timeUpdate) {
                    ++index
                    scope.launch(Dispatchers.Main) {
                        amplitudeCallback.invoke(
                            mediaRecorder.maxAmplitude,
                            offset
                        )
                    }
                } else {
                    scope.launch(Dispatchers.Main) {
                        amplitudeCallback.invoke(
                            -1,
                            offset
                        )
                    }
                }
                delay(16)
            }
        }
        recorderStarted = true
    }

    fun stopRecorder() = scope.launch(mediaContext) {
        if (recorderStarted) {
            recordingProgress?.cancel()
            mediaRecorder.stop()
        }
        recorderStarted = false
    }

    fun preparePlayer(audioFile: String) = scope.launch(mediaContext) {
        mediaPlayer.reset()
        mediaPlayer.setDataSource(audioFile)
        mediaPlayer.prepare()
    }

    var playerPosition: Deferred<Unit>? = null

    fun startPlayer() = scope.launch(mediaContext) {
        playerPosition = scope.async(mediaContext) {
            while (mediaPlayer.isPlaying) {
                val progress = mediaPlayer.currentPosition.toFloat() / mediaPlayer.duration
                scope.launch(Dispatchers.Main) {
                    playProgressListener.invoke(progress)
                }
                delay(10)
            }
        }
        mediaPlayer.setOnCompletionListener {
            playerPosition?.cancel()
            scope.launch(Dispatchers.Main) {
                playFinishedCallback.invoke()
            }
        }
        if (!mediaPlayer.isPlaying)
            mediaPlayer.start()

    }

    fun stopPlayer() = scope.launch(mediaContext) {
        if (mediaPlayer.isPlaying) {
            playerPosition?.cancelAndJoin()
            scope.launch(Dispatchers.Main) {
                playProgressListener.invoke(1f)
            }
            mediaPlayer.pause()
            mediaPlayer.seekTo(0)
        }
    }

    fun release() = scope.launch(mediaContext) {
        mediaRecorder.release()
        mediaPlayer.release()
        GlobalScope.launch(Dispatchers.Main) {
            mediaContext.close()
            scope.cancel()
        }
    }

}