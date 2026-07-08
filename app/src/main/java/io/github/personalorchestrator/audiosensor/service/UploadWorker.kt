package io.github.personalorchestrator.audiosensor.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.personalorchestrator.audiosensor.data.source.AudioApi
import io.github.personalorchestrator.audiosensor.data.repository.AudioSyncRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

class UploadWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params), KoinComponent {

    private val api: AudioApi by inject()
    private val repository: AudioSyncRepository by inject()

    override suspend fun doWork(): Result {
        val directory = repository.getOutputDirectory()
        val files = directory.listFiles { _, name -> name.endsWith(".m4a") }
            ?.sortedBy { it.name } ?: return Result.success()

        if (files.isEmpty()) return Result.success()

        var allSuccess = true
        for (file in files) {
            try {
                val requestFile = file.asRequestBody("audio/mp4".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                val response = api.uploadAudio(body)

                if (response.isSuccessful) {
                    file.delete()
                } else {
                    allSuccess = false
                    break
                }
            } catch (_: Exception) {
                allSuccess = false
                break
            }
        }

        return if (allSuccess) {
            Result.success()
        } else {
            Result.retry()
        }
    }
}
