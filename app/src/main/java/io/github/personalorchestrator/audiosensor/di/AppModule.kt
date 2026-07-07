package io.github.personalorchestrator.audiosensor.di

import io.github.personalorchestrator.audiosensor.data.repository.AudioSyncRepository
import io.github.personalorchestrator.audiosensor.data.source.AudioRecorder
import io.github.personalorchestrator.audiosensor.data.source.NetworkClient
import io.github.personalorchestrator.audiosensor.ui.viewmodel.RecordViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single { AudioRecorder(get()) }
    single { AudioSyncRepository(get()) }
    single { NetworkClient.createApi() }
    viewModelOf(::RecordViewModel)
}
