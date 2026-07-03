package com.example.wearos_audio_sensor.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.example.wearos_audio_sensor.R
import com.example.wearos_audio_sensor.ui.viewmodel.RecordingState
import com.example.wearos_audio_sensor.ui.viewmodel.RecordViewModel

@Composable
fun MainRecordScreen(viewModel: RecordViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        timeText = { TimeText() }
    ) {
        Button(
            onClick = { viewModel.toggleRecording() },
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            enabled = uiState != RecordingState.SYNCING
        ) {
            val text = when (uiState) {
                RecordingState.IDLE -> stringResource(R.string.record)
                RecordingState.RECORDING -> stringResource(R.string.stop)
                RecordingState.SYNCING -> stringResource(R.string.syncing)
            }
            Text(
                text = text,
                style = MaterialTheme.typography.button
            )
        }
    }
}
