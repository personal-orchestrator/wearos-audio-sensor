package com.example.wearos_audio_sensor

import com.example.wearos_audio_sensor.data.repository.AudioSyncRepository
import com.example.wearos_audio_sensor.data.source.AudioRecorder
import com.example.wearos_audio_sensor.ui.viewmodel.RecordingState
import com.example.wearos_audio_sensor.ui.viewmodel.RecordViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class RecordViewModelTest {

    private val audioRecorder = mockk<AudioRecorder>(relaxed = true)
    private val repository = mockk<AudioSyncRepository>(relaxed = true)
    private lateinit var viewModel: RecordViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = RecordViewModel(audioRecorder, repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `toggleRecording from IDLE should start recording`() = runTest {
        val file = File("test.m4a")
        every { repository.createNewFile() } returns file

        viewModel.toggleRecording()

        coVerify { audioRecorder.start(file) }
        assertEquals(RecordingState.RECORDING, viewModel.uiState.value)
    }

    @Test
    fun `toggleRecording from RECORDING should stop recording and schedule upload`() = runTest {
        val file = File("test.m4a")
        every { repository.createNewFile() } returns file
        
        // Start recording first
        viewModel.toggleRecording()
        
        // Toggle again to stop
        viewModel.toggleRecording()

        coVerify { audioRecorder.stop() }
        verify { repository.scheduleUpload(file) }
        assertEquals(RecordingState.IDLE, viewModel.uiState.value)
    }
}
