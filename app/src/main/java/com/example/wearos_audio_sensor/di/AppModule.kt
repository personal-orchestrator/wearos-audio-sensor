package com.example.wearos_audio_sensor.di

import com.example.wearos_audio_sensor.data.repository.AudioSyncRepository
import com.example.wearos_audio_sensor.data.source.AudioRecorder
import com.example.wearos_audio_sensor.data.source.NetworkClient
import com.example.wearos_audio_sensor.ui.viewmodel.RecordViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { AudioRecorder(get()) }
    single { AudioSyncRepository(get()) }
    single { NetworkClient.createApi() }
    viewModel { RecordViewModel(get(), get()) }
}
