package io.github.personalorchestrator.audiosensor.data.repository

import android.content.Context
import androidx.work.*
import io.github.personalorchestrator.audiosensor.service.UploadWorker
import java.io.File
import java.util.UUID

class AudioSyncRepository(private val context: Context) {

    fun getOutputDirectory(): File {
        val dir = File(context.cacheDir, "recordings")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun createNewFile(): File {
        val timestamp = System.currentTimeMillis()
        val uuid = UUID.randomUUID().toString()
        return File(getOutputDirectory(), "rec_${timestamp}_$uuid.m4a")
    }

    fun scheduleUpload() {
        val uploadRequest = OneTimeWorkRequestBuilder<UploadWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                java.util.concurrent.TimeUnit.MILLISECONDS,
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "AUDIO_SYNC_WORK",
            ExistingWorkPolicy.REPLACE,
            uploadRequest
        )
    }
}
