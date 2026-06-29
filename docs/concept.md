# **Role & Context**

You are an expert Principal Android Developer specializing in low-power Wear OS edge-computing applications. You write clean, production-ready Kotlin code adhering to modern Android development standards, strict separation of concerns, and defensive error-handling.

# **Project Objective**

Build a lightweight, single-purpose Wear OS application named wearos-audio-sensor.  
The app functions strictly as an **Edge Sensor** designed to record raw user voice dictation, save it to local storage as an compressed AAC (.m4a) audio file, and reliably forward it to an external backend API via an asynchronous background synchronization loop.

## **1\. Core Architecture: Store-and-Forward Pattern**

The application must handle frequent network drops (Wi-Fi/Bluetooth disconnects) seamlessly without losing user data or blocking the user interface.

1. **Local Storage (Store):** Audio must be encoded using hardware-efficient AAC into an .m4a format and streamed directly to a temporary file on the watch's local disk storage.
2. **Background Upload (Forward):** Once recording stops, an asynchronous background routine must queue the file for network transmission.
3. **Fault Tolerance:** If the server is unreachable, the file must remain safely cached on the device disk, retrying up to 5 times using exponential backoff until a successful HTTP 200/202 status code is received from the backend, after which the local file is purged.

## **2\. Project Layout & Component Organization**

Organize the codebase into a clean, modern package structure:  
wearos-audio-sensor/  
├── data/  
│ ├── repository/  
│ │ └── AudioSyncRepository.kt \# Manages file uploads and queue status  
│ └── source/  
│ ├── AudioRecorder.kt \# Native Android MediaRecorder wrapper (AAC/.m4a)  
│ └── NetworkClient.kt \# OkHttp/Retrofit network initialization  
├── di/  
│ └── AppModule.kt \# Basic Dependency Injection configuration  
├── service/  
│ └── UploadWorker.kt \# Android WorkManager task for reliable background sync  
├── ui/  
│ ├── viewmodel/  
│ │ └── RecordViewModel.kt \# UI State controller (Idle, Recording, Uploading)  
│ └── screen/  
│ └── MainRecordScreen.kt \# Jetpack Compose Wear OS UI element  
└── MainActivity.kt \# Entry point (Requesting permissions, inflating UI)

## **3\. Technical Requirements & Implementation Details**

### **A. UI Layer (Jetpack Compose for Wear OS)**

* Keep it absolute minimalist and highly glanceable. No complex material navigation.
* Provide a **single black screen** dominated by one massive, centered **Record Button**.
* **Visual States:**
  * *Idle:* Displays a grey microphone icon or text ("Tap to Record").
  * *Recording:* Button turns bright red, displaying a simple counter or waveform indicator ("Recording...").
  * *Syncing:* A subtle loading indicator appears at the top perimeter indicating a background sync is queued.

### **B. Audio Recording Implementation (AudioRecorder.kt)**

* Utilize Android's native MediaRecorder API.
* Configure it to utilize the hardware encoder for maximum battery optimization:
  * Audio Source: MediaRecorder.AudioSource.MIC
  * Output Format: MediaRecorder.OutputFormat.MPEG\_4
  * Audio Encoder: MediaRecorder.AudioEncoder.AAC
  * Sampling Rate: 16000 Hz (Optimized for Whisper AI ingestion)
  * Encoding Bit Rate: 32000 bps
* **State Machine Hardening:** Introduce an artificial 500-millisecond delay when the user stops the recording before actually invoking MediaRecorder.stop(). This ensures the hardware buffer fully flushes the acoustic data to the disk before finalizing the MPEG-4 container, completely neutralizing the known AAC truncation bug1.
* Save files dynamically to the app's internal cache directory (context.cacheDir) using clean timestamps: audio\_recording\_YYYYMMDD\_HHMMSS.m4a.

### **C. Networking & Sync Layer (UploadWorker.kt)**

* Implement the background sync task using **Android WorkManager**. This ensures execution even if the user exits the application.
* **Expedited Execution:** Configure the WorkRequest using setExpedited(OutOfQuotaPolicy.RUN\_AS\_NON\_EXPEDITED\_WORK\_REQUEST). This elevated system priority allows the upload to bypass aggressive Wear OS battery policies (like Doze mode) and execute quickly in the background2.
* **Network Routing:** Rely on the default Wear OS network routing. Because the target endpoint is an external domain, the watch will automatically tunnel the encrypted request through the paired phone's Bluetooth internet connection, handling public DNS resolution and basic authentication natively without needing to force the Wi-Fi radio awake3.
* **Network Payload:** Perform a standard multipart HTTP POST request targeting the external HTTPS ingestion endpoint: POST https://\<external-domain\>/api/v1/ingest/audio, passing the required Basic Auth headers.
* **Constraints:** Configure the WorkManager task to execute *only* when network connectivity is actively available (NetworkType.CONNECTED).

# **Execution Order**

Please generate the complete codebase starting with the data layer (AudioRecorder.kt and UploadWorker.kt), followed by the RecordViewModel.kt, and finally the Jetpack Compose MainRecordScreen.kt. Include proper manifest permissions for RECORD\_AUDIO and INTERNET.

#### **Works cited**

1. MediaRecorder cuts off end of file \- Stack Overflow, [https://stackoverflow.com/questions/15886416/mediarecorder-cuts-off-end-of-file](https://stackoverflow.com/questions/15886416/mediarecorder-cuts-off-end-of-file)
2. Define work requests | Background work \- Android Developers, [https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started/define-work](https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started/define-work)
3. Wear OS: Can't connect to Home Assistant instance over Bluetooth · Issue \#1866 \- GitHub, [https://github.com/home-assistant/android/issues/1866](https://github.com/home-assistant/android/issues/1866)
4. Wear OS: Support switching between local and cloud URL \#3773 \- GitHub, [https://github.com/home-assistant/android/issues/3773](https://github.com/home-assistant/android/issues/3773)