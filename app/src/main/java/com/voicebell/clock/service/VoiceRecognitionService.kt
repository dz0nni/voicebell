package com.voicebell.clock.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.voicebell.clock.R
import com.voicebell.clock.util.VoiceCommandParser
import com.voicebell.clock.vosk.VoskWrapper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext
import org.json.JSONObject
import java.io.File
import javax.inject.Inject

/**
 * Foreground service for offline voice recognition using Vosk.
 *
 * This service:
 * - Manages microphone access
 * - Processes audio through Vosk
 * - Parses voice commands
 * - Broadcasts results to the UI
 *
 * Based on the original architecture plan with push-to-talk pattern.
 */
@AndroidEntryPoint
class VoiceRecognitionService : Service() {

    @Inject
    lateinit var voskWrapper: VoskWrapper

    @Inject
    lateinit var voiceCommandParser: VoiceCommandParser

    @Inject
    lateinit var voskModelManager: com.voicebell.clock.util.VoskModelManager

    private var audioRecord: AudioRecord? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var recordingJob: Job? = null

    private var isRecording = false

    companion object {
        private const val TAG = "VoiceRecognitionService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "voice_recognition_channel"
        private const val CHANNEL_NAME = "Voice Recognition"

        private const val SAMPLE_RATE = 16000
        private const val CHANNEL = AudioFormat.CHANNEL_IN_MONO
        private const val ENCODING = AudioFormat.ENCODING_PCM_16BIT

        const val ACTION_START_LISTENING = "com.voicebell.clock.START_LISTENING"
        const val ACTION_STOP_LISTENING = "com.voicebell.clock.STOP_LISTENING"
        const val ACTION_RESULT = "com.voicebell.clock.VOICE_RESULT"
        const val EXTRA_RESULT_TEXT = "result_text"
        const val EXTRA_RESULT_SUCCESS = "result_success"

        private const val MODEL_DIR_NAME = "vosk-model"

        fun startListening(context: Context) {
            val intent = Intent(context, VoiceRecognitionService::class.java).apply {
                action = ACTION_START_LISTENING
            }
            context.startService(intent)
        }

        fun stopListening(context: Context) {
            val intent = Intent(context, VoiceRecognitionService::class.java).apply {
                action = ACTION_STOP_LISTENING
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")

        // Initialize Vosk model
        initializeVoskModel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_LISTENING -> {
                Log.d(TAG, "Start listening action received")
                startForeground(NOTIFICATION_ID, createNotification("Listening..."))
                startRecording()
            }
            ACTION_STOP_LISTENING -> {
                Log.d(TAG, "Stop listening action received")
                stopRecording()
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")

        stopRecording()
        voskWrapper.release()
        serviceScope.cancel()
    }

    /**
     * Initialize Vosk model from internal storage.
     * If model doesn't exist, extract it from assets first.
     */
    private fun initializeVoskModel() {
        serviceScope.launch {
            try {
                // Check if model is already extracted
                if (!voskModelManager.isModelDownloaded()) {
                    Log.i(TAG, "Model not found, extracting from assets...")
                    val extractResult = voskModelManager.extractModelFromAssets()

                    if (extractResult.isFailure) {
                        Log.e(TAG, "Failed to extract model from assets", extractResult.exceptionOrNull())
                        broadcastResult(null, false, "Failed to load voice model from assets")
                        return@launch
                    }

                    Log.i(TAG, "Model extracted successfully")
                }

                val modelPath = voskModelManager.getModelPath()
                Log.d(TAG, "Initializing Vosk model from: $modelPath")

                val initialized = voskWrapper.initModel(modelPath)

                if (!initialized) {
                    Log.e(TAG, "Failed to initialize Vosk model")
                    broadcastResult(null, false, "Failed to load voice model")
                } else {
                    Log.i(TAG, "Vosk model initialized successfully")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing Vosk model", e)
                broadcastResult(null, false, "Error loading voice model: ${e.message}")
            }
        }
    }

    /**
     * Start recording audio from microphone.
     */
    private fun startRecording() {
        if (isRecording) {
            Log.w(TAG, "Already recording")
            return
        }

        if (!voskWrapper.isInitialized()) {
            Log.e(TAG, "Vosk wrapper not initialized")
            broadcastResult(null, false, "Voice recognition not ready")
            return
        }

        try {
            val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, ENCODING)

            if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                Log.e(TAG, "Invalid buffer size: $bufferSize")
                broadcastResult(null, false, "Microphone error")
                return
            }

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                SAMPLE_RATE,
                CHANNEL,
                ENCODING,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord not initialized")
                broadcastResult(null, false, "Microphone not available")
                return
            }

            audioRecord?.startRecording()
            isRecording = true

            // Reset recognizer for new session
            voskWrapper.reset()

            // Start processing audio in background
            recordingJob = serviceScope.launch {
                processAudio(bufferSize)
            }

            Log.i(TAG, "Recording started")

        } catch (e: SecurityException) {
            Log.e(TAG, "Microphone permission denied", e)
            broadcastResult(null, false, "Microphone permission required")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting recording", e)
            broadcastResult(null, false, "Failed to start recording")
        }
    }

    /**
     * Stop recording audio.
     */
    private fun stopRecording() {
        if (!isRecording) {
            return
        }

        isRecording = false
        recordingJob?.cancel()

        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null

            // Get final result from Vosk
            val finalResult = voskWrapper.finalResult()
            processFinalResult(finalResult)

            Log.i(TAG, "Recording stopped")

        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording", e)
        }
    }

    /**
     * Process audio chunks from microphone.
     */
    private suspend fun processAudio(bufferSize: Int) {
        val buffer = ByteArray(bufferSize)

        while (coroutineContext.isActive && isRecording) {
            try {
                val readBytes = audioRecord?.read(buffer, 0, buffer.size) ?: 0

                if (readBytes > 0) {
                    // Send audio to Vosk
                    val result = voskWrapper.acceptAudioChunk(buffer)

                    // If final result available, process it
                    if (result != null) {
                        processFinalResult(result)
                        // Stop after getting result (push-to-talk pattern)
                        stopRecording()
                        break
                    }
                } else if (readBytes < 0) {
                    Log.e(TAG, "Error reading audio: $readBytes")
                    break
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error processing audio", e)
                break
            }
        }
    }

    /**
     * Process final recognition result.
     */
    private fun processFinalResult(jsonResult: String?) {
        if (jsonResult.isNullOrBlank()) {
            Log.w(TAG, "Empty recognition result")
            broadcastResult(null, false, "No speech detected")
            return
        }

        try {
            // Parse JSON result from Vosk
            val json = JSONObject(jsonResult)
            val text = json.optString("text", "").trim()

            if (text.isEmpty()) {
                Log.w(TAG, "No text in result")
                broadcastResult(null, false, "Could not understand")
                return
            }

            Log.i(TAG, "Recognized text: $text")

            // Parse voice command
            val commandResult = voiceCommandParser.parseCommand(text)

            // Broadcast result to UI
            broadcastResult(text, true, null)

        } catch (e: Exception) {
            Log.e(TAG, "Error processing result", e)
            broadcastResult(null, false, "Processing error")
        }
    }

    /**
     * Broadcast recognition result to the app.
     */
    private fun broadcastResult(text: String?, success: Boolean, errorMessage: String?) {
        val intent = Intent(ACTION_RESULT).apply {
            putExtra(EXTRA_RESULT_TEXT, text ?: errorMessage ?: "Unknown error")
            putExtra(EXTRA_RESULT_SUCCESS, success)
        }
        sendBroadcast(intent)
    }

    /**
     * Create notification for foreground service.
     */
    private fun createNotification(contentText: String): Notification {
        createNotificationChannel()

        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Voice Recognition")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    /**
     * Create notification channel (Android 8.0+).
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Voice recognition service"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
