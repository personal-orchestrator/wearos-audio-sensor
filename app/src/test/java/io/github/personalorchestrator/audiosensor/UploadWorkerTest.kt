package io.github.personalorchestrator.audiosensor

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.github.personalorchestrator.audiosensor.data.source.AudioApi
import io.github.personalorchestrator.audiosensor.service.UploadWorker
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
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

    @Before
    fun setup() {
        startKoin {
            modules(
                module {
                    single { api }
                },
            )
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `doWork returns success when upload succeeds`() = runTest {
        val file = File.createTempFile("test", ".m4a")
        every { workerParams.inputData } returns workDataOf("file_path" to file.absolutePath)
        coEvery { api.uploadAudio(any()) } returns Response.success(Unit)

        val worker = UploadWorker(context, workerParams)
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertEquals(false, file.exists())
    }

    @Test
    fun `doWork returns retry when upload fails`() = runTest {
        val file = File.createTempFile("test", ".m4a")
        every { workerParams.inputData } returns workDataOf("file_path" to file.absolutePath)
        coEvery { api.uploadAudio(any()) } returns Response.error(500, mockk(relaxed = true))

        val worker = UploadWorker(context, workerParams)
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
        assertEquals(true, file.exists())
        file.delete()
    }
}
