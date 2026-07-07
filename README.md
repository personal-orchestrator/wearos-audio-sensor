# wearos-audio-sensor
A custom Wear OS edge sensor that captures raw audio dictation and securely pushes it to the ingestion server.

## Architecture: Store-and-Forward Pattern

The application is designed to handle frequent network drops (Wi-Fi/Bluetooth disconnects) common in Wear OS environments.

1.  **Local Storage (Store)**: Audio is encoded using hardware-efficient AAC into `.m4a` format and streamed directly to temporary storage.
2.  **Background Upload (Forward)**: Once recording stops, an asynchronous background routine queues the file for network transmission.
3.  **Fault Tolerance**: If the server is unreachable, files remain safely cached, retrying up to 5 times with exponential backoff via WorkManager.

## Component Organization

-   `data/source`: Native `AudioRecorder` (AAC/.m4a) and `NetworkClient` (Retrofit).
-   `data/repository`: `AudioSyncRepository` managing file uploads and queue status.
-   `service`: `UploadWorker` for reliable background sync.
-   `ui`: `RecordViewModel` (UI State) and `MainRecordScreen` (Jetpack Compose).
-   `di`: Koin-based dependency injection.

## Technical Details

-   **AAC Buffer Flushing**: Includes an artificial 500ms delay when stopping recording to ensure the hardware buffer fully flushes, neutralizing the known AAC truncation bug.
-   **Expedited Work**: Uses WorkManager's expedited execution to bypass aggressive Wear OS battery policies.
-   **Network Routing**: Relies on default Wear OS routing to tunnel requests through the paired phone's connection.
