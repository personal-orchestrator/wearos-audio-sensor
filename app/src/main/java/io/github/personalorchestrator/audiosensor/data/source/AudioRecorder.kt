package io.github.personalorchestrator.audiosensor.data.source

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.time.Duration.Companion.milliseconds

class AudioRecorder(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null

    suspend fun start(outputFile: File) = withContext(Dispatchers.IO) {
        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(16000)
            setOutputFile(FileOutputStream(outputFile).fd)
            prepare()
            start()
        }
        mediaRecorder = recorder
    }

    suspend fun stop() = withContext(Dispatchers.IO) {
        // 500ms delay to prevent truncation of the last words in AAC
        delay(500.milliseconds)
        mediaRecorder?.apply {
            stop()
            reset()
            release()
        }
        mediaRecorder = null
    }
}
