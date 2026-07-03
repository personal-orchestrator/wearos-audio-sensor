package com.example.wearos_audio_sensor

import android.content.Context
import androidx.work.WorkManager
import com.example.wearos_audio_sensor.data.repository.AudioSyncRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class AudioSyncRepositoryTest {

    private val context = mockk<Context>(relaxed = true)
    private val workManager = mockk<WorkManager>(relaxed = true)
    private lateinit var repository: AudioSyncRepository

    @Before
    fun setup() {
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(any()) } returns workManager
        val cacheDir = File("build/test-cache")
        cacheDir.mkdirs()
        every { context.cacheDir } returns cacheDir
        repository = AudioSyncRepository(context)
    }

    @Test
    fun `createNewFile should create a file in recordings directory`() {
        val file = repository.createNewFile()
        assertTrue(file.path.contains("recordings"))
        assertTrue(file.name.startsWith("rec_"))
        assertTrue(file.name.endsWith(".m4a"))
    }

    @Test
    fun `scheduleUpload should enqueue unique work`() {
        val file = File("test.m4a")
        repository.scheduleUpload(file)
        verify { workManager.enqueueUniqueWork(file.name, any(), any<androidx.work.OneTimeWorkRequest>()) }
    }
}
