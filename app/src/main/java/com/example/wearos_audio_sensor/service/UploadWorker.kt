package com.example.wearos_audio_sensor.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.wearos_audio_sensor.data.source.AudioApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

class UploadWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {

    private val api: AudioApi by inject()

    override suspend fun doWork(): Result {
        val filePath = inputData.getString("file_path") ?: return Result.failure()
        val file = File(filePath)

        if (!file.exists()) return Result.failure()

        return try {
            val requestFile = file.asRequestBody("audio/mp4".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val response = api.uploadAudio(body)

            if (response.isSuccessful) {
                file.delete()
                Result.success()
            } else {
                Result.retry()
            }
        } catch (ignored: Exception) {
            Result.retry()
        }
    }
}
