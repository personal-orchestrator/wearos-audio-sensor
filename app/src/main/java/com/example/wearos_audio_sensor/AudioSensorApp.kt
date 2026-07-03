package com.example.wearos_audio_sensor

import android.app.Application
import androidx.work.Configuration
import com.example.wearos_audio_sensor.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.factory.KoinWorkerFactory
import org.koin.core.context.startKoin

class AudioSensorApp : Application(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@AudioSensorApp)
            modules(appModule)
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(KoinWorkerFactory())
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
}
