package com.example.wearos_audio_sensor.data.source

import okhttp3.Credentials
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import com.example.wearos_audio_sensor.BuildConfig

interface AudioApi {
    @Multipart
    @POST("upload")
    suspend fun uploadAudio(
        @Part audio: MultipartBody.Part
    ): Response<Unit>
}

object NetworkClient {
    private const val BASE_URL = BuildConfig.INGESTION_URL

    fun createApi(baseUrl: String = BASE_URL): AudioApi {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val auth = Credentials.basic(BuildConfig.INGESTION_USER, BuildConfig.INGESTION_PASSWORD)
                val request = chain.request().newBuilder()
                    .header("Authorization", auth)
                    .build()
                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(AudioApi::class.java)
    }
}
