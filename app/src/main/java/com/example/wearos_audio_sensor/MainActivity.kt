package com.example.wearos_audio_sensor

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.example.wearos_audio_sensor.ui.screen.MainRecordScreen
import com.example.wearos_audio_sensor.ui.viewmodel.RecordViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val viewModel: RecordViewModel by viewModel()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // In a real app, show a message explaining why the permission is needed
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)

        setContent {
            MainRecordScreen(viewModel = viewModel)
        }
    }
}
