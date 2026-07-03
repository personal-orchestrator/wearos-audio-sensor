package com.example.wearos_audio_sensor.data.repository

import android.content.Context
import androidx.work.*
import com.example.wearos_audio_sensor.service.UploadWorker
import java.io.File
import java.util.UUID

class AudioSyncRepository(private val context: Context) {

    fun getOutputDirectory(): File {
        val dir = File(context.cacheDir, "recordings")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun createNewFile(): File {
        return File(getOutputDirectory(), "rec_${System.currentTimeMillis()}.m4a")
    }

    fun scheduleUpload(file: File) {
        val uploadRequest = OneTimeWorkRequestBuilder<UploadWorker>()
            .setInputData(workDataOf("file_path" to file.absolutePath))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                java.util.concurrent.TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            file.name,
            ExistingWorkPolicy.REPLACE,
            uploadRequest
        )
    }
}
