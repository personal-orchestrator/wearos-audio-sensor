package io.github.personalorchestrator.audiosensor

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import io.github.personalorchestrator.audiosensor.data.source.AudioApi
import io.github.personalorchestrator.audiosensor.data.repository.AudioSyncRepository
import io.github.personalorchestrator.audiosensor.service.UploadWorker
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import retrofit2.Response
import java.io.File

class UploadWorkerTest {

    private val context = mockk<Context>(relaxed = true)
    private val workerParams = mockk<WorkerParameters>(relaxed = true)
    private val api = mockk<AudioApi>()
    private val repository = mockk<AudioSyncRepository>()
    private lateinit var testDir: File

    @Before
    fun setup() {
        testDir = File("build/test-recordings").apply {
            deleteRecursively()
            mkdirs()
        }
        every { repository.getOutputDirectory() } returns testDir

        startKoin {
            modules(
                module {
                    single { api }
                    single { repository }
                },
            )
        }
    }

    @After
    fun tearDown() {
        stopKoin()
        testDir.deleteRecursively()
    }

    @Test
    fun `doWork returns success when all uploads succeed`() = runTest {
        val file1 = File(testDir, "file1.m4a").apply { writeText("data1") }
        val file2 = File(testDir, "file2.m4a").apply { writeText("data2") }
        
        coEvery { api.uploadAudio(any()) } returns Response.success(Unit)

        val worker = UploadWorker(context, workerParams)
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertEquals(false, file1.exists())
        assertEquals(false, file2.exists())
        coVerify(exactly = 2) { api.uploadAudio(any()) }
    }

    @Test
    fun `doWork returns retry and stops when an upload fails`() = runTest {
        val file1 = File(testDir, "file1.m4a").apply { writeText("data1") }
        val file2 = File(testDir, "file2.m4a").apply { writeText("data2") }
        
        coEvery { api.uploadAudio(any()) } returns Response.error(500, mockk(relaxed = true))

        val worker = UploadWorker(context, workerParams)
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
        assertEquals(true, file1.exists())
        assertEquals(true, file2.exists())
        coVerify(exactly = 1) { api.uploadAudio(any()) }
    }

    @Test
    fun `doWork skips non-m4a files`() = runTest {
        val file1 = File(testDir, "file1.m4a").apply { writeText("data1") }
        val otherFile = File(testDir, "other.txt").apply { writeText("text") }
        
        coEvery { api.uploadAudio(any()) } returns Response.success(Unit)

        val worker = UploadWorker(context, workerParams)
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertEquals(false, file1.exists())
        assertEquals(true, otherFile.exists())
        coVerify(exactly = 1) { api.uploadAudio(any()) }
    }
}
