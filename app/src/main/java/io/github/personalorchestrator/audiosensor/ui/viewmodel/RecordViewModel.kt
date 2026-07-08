package io.github.personalorchestrator.audiosensor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.personalorchestrator.audiosensor.data.repository.AudioSyncRepository
import io.github.personalorchestrator.audiosensor.data.source.AudioRecorder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

enum class RecordingState {
    IDLE, RECORDING, SYNCING
}

class RecordViewModel(
    private val audioRecorder: AudioRecorder,
    private val repository: AudioSyncRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordingState.IDLE)
    val uiState: StateFlow<RecordingState> = _uiState

    private var currentFile: File? = null

    fun toggleRecording() {
        viewModelScope.launch {
            if (_uiState.value == RecordingState.RECORDING) {
                stopRecording()
            } else {
                startRecording()
            }
        }
    }

    private suspend fun startRecording() {
        val file = repository.createNewFile()
        currentFile = file
        _uiState.value = RecordingState.RECORDING
        try {
            audioRecorder.start(file)
        } catch (_: Exception) {
            _uiState.value = RecordingState.IDLE
        }
    }

    private suspend fun stopRecording() {
        _uiState.value = RecordingState.SYNCING
        try {
            audioRecorder.stop()
            repository.scheduleUpload()
        } finally {
            _uiState.value = RecordingState.IDLE
            currentFile = null
        }
    }
}
