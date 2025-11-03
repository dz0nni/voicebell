package com.voicebell.clock.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.voicebell.clock.MainActivity
import com.voicebell.clock.R
import com.voicebell.clock.util.NotificationHelper
import com.voicebell.clock.util.VoiceCommandParser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
import java.io.File
import java.io.IOException
import javax.inject.Inject

/**
 * Foreground service for handling offline voice recognition with Vosk.
 *
 * This service:
 * - Loads Vosk model (40MB small-en-us model)
 * - Records audio from microphone
 * - Recognizes speech offline using Vosk
 * - Parses commands and broadcasts results
 */
@AndroidEntryPoint
class VoiceRecognitionService : Service(), RecognitionListener {

    companion object {
        private const val TAG = "VoiceRecognitionService"

        const val ACTION_START_RECOGNITION = "com.voicebell.clock.START_RECOGNITION"
        const val ACTION_STOP_RECOGNITION = "com.voicebell.clock.STOP_RECOGNITION"

        const val EXTRA_RESULT_TEXT = "result_text"
        const val EXTRA_RESULT_TYPE = "result_type"

        const val BROADCAST_RECOGNITION_RESULT = "com.voicebell.clock.RECOGNITION_RESULT"
        const val BROADCAST_RECOGNITION_ERROR = "com.voicebell.clock.RECOGNITION_ERROR"

        private const val MODEL_NAME = "vosk-model-small-en-us-0.15"
        private const val SAMPLE_RATE = 16000f
    }

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var voiceCommandParser: VoiceCommandParser

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var speechService: SpeechService? = null
    private var model: Model? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_RECOGNITION -> {
                startForeground(
                    NotificationHelper.NOTIFICATION_ID_VOICE_SERVICE,
                    createForegroundNotification()
                )
                initializeVosk()
            }
            ACTION_STOP_RECOGNITION -> {
                stopRecognition()
                stopSelf()
            }
            else -> stopSelf()
        }

        return START_NOT_STICKY
    }

    /**
     * Initializes Vosk model and starts recognition.
     */
    private fun initializeVosk() {
        serviceScope.launch(Dispatchers.IO) {
            try {
                // Check if model exists, if not download/extract it
                val modelPath = File(getExternalFilesDir(null), MODEL_NAME)

                if (!modelPath.exists()) {
                    Log.i(TAG, "Model not found, downloading...")
                    // Extract model from assets or download
                    // For now, we assume model is in assets
                    extractModelFromAssets(modelPath)
                }

                if (!modelPath.exists()) {
                    Log.e(TAG, "Model file not found after extraction")
                    broadcastError("Voice model not available. Please download it from settings.")
                    stopSelf()
                    return@launch
                }

                // Initialize model
                model = Model(modelPath.absolutePath)

                // Start recognition
                withContext(Dispatchers.Main) {
                    startRecognition()
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error initializing Vosk", e)
                broadcastError("Failed to initialize voice recognition: ${e.message}")
                stopSelf()
            }
        }
    }

    /**
     * Extracts Vosk model from assets (if packaged).
     */
    private fun extractModelFromAssets(targetPath: File) {
        // Note: In production, the model should either be:
        // 1. Downloaded on first launch
        // 2. Packaged in assets (increases APK size significantly)
        // 3. Downloaded from a separate repository

        // For this implementation, we'll use StorageService from Vosk
        try {
            StorageService.unpack(
                assets,
                MODEL_NAME,
                targetPath.parentFile!!.absolutePath,
                { progress ->
                    Log.d(TAG, "Model extraction progress: $progress%")
                },
                { exception ->
                    Log.e(TAG, "Model extraction failed", exception)
                }
            )
        } catch (e: IOException) {
            Log.e(TAG, "Failed to extract model from assets", e)
        }
    }

    /**
     * Starts speech recognition.
     */
    private fun startRecognition() {
        try {
            val rec = Recognizer(model, SAMPLE_RATE)
            speechService = SpeechService(rec, SAMPLE_RATE)
            speechService?.startListening(this)
            Log.i(TAG, "Voice recognition started")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting recognition", e)
            broadcastError("Failed to start voice recognition: ${e.message}")
            stopSelf()
        }
    }

    /**
     * Stops speech recognition.
     */
    private fun stopRecognition() {
        speechService?.stop()
        speechService?.shutdown()
        speechService = null
        Log.i(TAG, "Voice recognition stopped")
    }

    /**
     * Creates foreground notification.
     */
    private fun createForegroundNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID_VOICE_SERVICE)
            .setContentTitle("Voice Command")
            .setContentText("Listening...")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO: Add proper icon
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    /**
     * Broadcasts recognition result.
     */
    private fun broadcastResult(text: String) {
        val intent = Intent(BROADCAST_RECOGNITION_RESULT).apply {
            putExtra(EXTRA_RESULT_TEXT, text)
        }
        sendBroadcast(intent)
        Log.d(TAG, "Broadcast result: $text")
    }

    /**
     * Broadcasts recognition error.
     */
    private fun broadcastError(message: String) {
        val intent = Intent(BROADCAST_RECOGNITION_ERROR).apply {
            putExtra(EXTRA_RESULT_TEXT, message)
        }
        sendBroadcast(intent)
        Log.e(TAG, "Broadcast error: $message")
    }

    // RecognitionListener interface implementation

    override fun onPartialResult(hypothesis: String?) {
        Log.d(TAG, "Partial result: $hypothesis")
    }

    override fun onResult(hypothesis: String?) {
        Log.d(TAG, "Final result: $hypothesis")

        if (hypothesis != null && hypothesis.isNotEmpty()) {
            // Extract actual text from JSON hypothesis
            val text = extractTextFromHypothesis(hypothesis)

            if (text.isNotEmpty()) {
                broadcastResult(text)
                stopRecognition()
                stopSelf()
            }
        }
    }

    override fun onFinalResult(hypothesis: String?) {
        Log.d(TAG, "Final result: $hypothesis")
        onResult(hypothesis)
    }

    override fun onError(exception: Exception?) {
        Log.e(TAG, "Recognition error", exception)
        broadcastError(exception?.message ?: "Unknown error occurred")
        stopSelf()
    }

    override fun onTimeout() {
        Log.w(TAG, "Recognition timeout")
        broadcastError("Voice recognition timed out")
        stopSelf()
    }

    /**
     * Extracts text from Vosk JSON hypothesis.
     * Hypothesis format: {"text":"hello world"}
     */
    private fun extractTextFromHypothesis(hypothesis: String): String {
        return try {
            val textPattern = """"text"\s*:\s*"([^"]+)"""".toRegex()
            textPattern.find(hypothesis)?.groupValues?.get(1)?.trim() ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing hypothesis", e)
            ""
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecognition()
        model?.close()
        model = null
        serviceScope.cancel()
        Log.i(TAG, "Service destroyed")
    }
}
