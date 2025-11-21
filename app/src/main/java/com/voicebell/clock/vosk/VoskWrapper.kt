package com.voicebell.clock.vosk

import android.content.Context
import android.util.Log
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wrapper around Vosk speech recognition library.
 *
 * This class encapsulates Vosk complexity and provides a simple interface
 * for speech recognition. Based on the original architecture plan.
 *
 * Sample rate: 16000 Hz (standard for Vosk models)
 */
@Singleton
class VoskWrapper @Inject constructor() {

    private var model: Model? = null
    private var recognizer: Recognizer? = null
    private var lastPartialText: String = ""

    companion object {
        private const val TAG = "VoskWrapper"
        private const val SAMPLE_RATE = 16000.0f
    }

    /**
     * Initialize Vosk model from the given path.
     *
     * @param modelPath Absolute path to the unpacked Vosk model directory
     * @return true if initialization successful, false otherwise
     */
    fun initModel(modelPath: String): Boolean {
        return try {
            val modelDir = File(modelPath)
            if (!modelDir.exists()) {
                Log.e(TAG, "Model directory does not exist: $modelPath")
                return false
            }

            Log.d(TAG, "Loading Vosk model from: $modelPath")
            model = Model(modelPath)
            recognizer = Recognizer(model, SAMPLE_RATE)

            Log.i(TAG, "Vosk model initialized successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Vosk model", e)
            false
        }
    }

    /**
     * Process audio chunk and return recognition result.
     *
     * @param audioData Raw audio data (PCM 16-bit, mono, 16kHz)
     * @return RecognitionResult with text and isFinal flag, or null if no result
     */
    fun acceptAudioChunk(audioData: ByteArray): RecognitionResult? {
        val rec = recognizer ?: run {
            Log.w(TAG, "Recognizer not initialized")
            return null
        }

        return try {
            if (rec.acceptWaveForm(audioData, audioData.size)) {
                // Final result available
                val result = rec.result
                Log.d(TAG, "Final result: $result")

                // Parse final result text
                try {
                    val resultJson = org.json.JSONObject(result)
                    val resultText = resultJson.optString("text", "").trim()
                    if (resultText.isNotEmpty()) {
                        return RecognitionResult(resultText, isFinal = true)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse final result", e)
                }
                null
            } else {
                // Partial result (ongoing recognition)
                val partial = rec.partialResult
                Log.v(TAG, "Partial result: $partial")

                // Save last partial text for fallback
                try {
                    val partialJson = org.json.JSONObject(partial)
                    val partialText = partialJson.optString("partial", "").trim()
                    if (partialText.isNotEmpty()) {
                        lastPartialText = partialText
                        Log.v(TAG, "Saved partial text: $lastPartialText")

                        // Return partial result for real-time processing
                        return RecognitionResult(partialText, isFinal = false)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse partial result", e)
                }

                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing audio chunk", e)
            null
        }
    }

    /**
     * Data class for recognition results.
     */
    data class RecognitionResult(
        val text: String,
        val isFinal: Boolean
    )

    /**
     * Get the final recognition result.
     * Call this when audio stream ends.
     *
     * @return Final recognized text
     */
    fun finalResult(): String? {
        val rec = recognizer ?: return null

        return try {
            val result = rec.finalResult
            Log.d(TAG, "Final result retrieved: $result")

            // If final result is empty, use last partial text as fallback
            try {
                val json = org.json.JSONObject(result)
                val text = json.optString("text", "").trim()

                if (text.isEmpty() && lastPartialText.isNotEmpty()) {
                    Log.i(TAG, "Final result empty, using last partial text: $lastPartialText")
                    // Create JSON with partial text as final text
                    val fallbackJson = org.json.JSONObject()
                    fallbackJson.put("text", lastPartialText)
                    return fallbackJson.toString()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to parse final result for fallback", e)
            }

            result
        } catch (e: Exception) {
            Log.e(TAG, "Error getting final result", e)
            null
        }
    }

    /**
     * Get partial (intermediate) recognition result.
     * Useful for displaying real-time transcription.
     *
     * @return Partial recognized text
     */
    fun getPartialResult(): String? {
        val rec = recognizer ?: return null

        return try {
            rec.partialResult
        } catch (e: Exception) {
            Log.e(TAG, "Error getting partial result", e)
            null
        }
    }

    /**
     * Reset the recognizer state.
     * Call this to start a new recognition session.
     */
    fun reset() {
        recognizer?.let {
            try {
                // Recreate recognizer with same model
                model?.let { m ->
                    recognizer = Recognizer(m, SAMPLE_RATE)
                    lastPartialText = "" // Clear saved partial text
                    Log.d(TAG, "Recognizer reset")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error resetting recognizer", e)
            }
        }
    }

    /**
     * Check if the model is initialized.
     *
     * @return true if model is loaded and ready
     */
    fun isInitialized(): Boolean {
        return model != null && recognizer != null
    }

    /**
     * Release resources.
     * Call this when voice recognition is no longer needed.
     */
    fun release() {
        try {
            recognizer?.close()
            recognizer = null

            model?.close()
            model = null

            Log.i(TAG, "Vosk resources released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing Vosk resources", e)
        }
    }
}
