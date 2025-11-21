package com.voicebell.clock.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import com.voicebell.clock.R
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Handles playing timer notification sounds to Bluetooth headphones.
 *
 * Features:
 * - Ducks/pauses currently playing audio
 * - Plays sequential audio files (plinn.ogg -> hey-your-timer-is-finished.ogg)
 * - Routes audio to Bluetooth headphones
 * - Restores previous audio after playback
 */
class TimerAudioPlayer(private val context: Context) {

    companion object {
        private const val TAG = "TimerAudioPlayer"
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var mediaPlayer: MediaPlayer? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    /**
     * Play timer finished notification sounds to Bluetooth headphones.
     *
     * Sequence:
     * 1. Request audio focus (duck other audio)
     * 2. Play plinn.ogg (attention sound)
     * 3. Play hey-your-timer-is-finished.ogg (notification)
     * 4. Release audio focus (restore other audio)
     *
     * @param onComplete Callback invoked when all audio playback is finished
     */
    suspend fun playTimerFinishedSound(onComplete: () -> Unit) {
        Log.d(TAG, "Starting timer finished sound playback")

        try {
            // Request audio focus to duck other audio
            val focusGranted = requestAudioFocus()
            if (!focusGranted) {
                Log.w(TAG, "Audio focus not granted, playing anyway")
            }

            // Play plinn.ogg (attention sound)
            playAudioFile(R.raw.plinn)

            // Wait a bit between sounds
            kotlinx.coroutines.delay(200)

            // Play hey-your-timer-is-finished.ogg (notification)
            playAudioFile(R.raw.hey_your_timer_is_finished)

            Log.d(TAG, "Timer finished sound playback completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error playing timer finished sound", e)
        } finally {
            // Release audio focus to restore other audio
            releaseAudioFocus()
            onComplete()
        }
    }

    /**
     * Play a single audio file and wait for completion.
     */
    private suspend fun playAudioFile(resourceId: Int) = suspendCancellableCoroutine<Unit> { continuation ->
        try {
            // Release any existing MediaPlayer
            mediaPlayer?.release()

            // Create new MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )

                // Set data source from resources
                val afd = context.resources.openRawResourceFd(resourceId)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()

                // Prepare and play
                prepare()

                setOnCompletionListener {
                    Log.d(TAG, "Audio file playback completed: $resourceId")
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                }

                setOnErrorListener { mp, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                    true
                }

                start()
                Log.d(TAG, "Started playing audio file: $resourceId")
            }

            // Handle cancellation
            continuation.invokeOnCancellation {
                mediaPlayer?.apply {
                    if (isPlaying) {
                        stop()
                    }
                    release()
                }
                mediaPlayer = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing audio file: $resourceId", e)
            if (continuation.isActive) {
                continuation.resume(Unit)
            }
        }
    }

    /**
     * Request audio focus to duck other audio.
     */
    private fun requestAudioFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8+: Use AudioFocusRequest
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .build()

            audioFocusRequest = request
            val result = audioManager.requestAudioFocus(request)
            result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            val result = audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_ALARM,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            )
            result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    /**
     * Release audio focus to restore other audio.
     */
    private fun releaseAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { request ->
                audioManager.abandonAudioFocusRequest(request)
            }
            audioFocusRequest = null
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }

    /**
     * Release all resources.
     */
    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        releaseAudioFocus()
    }
}
