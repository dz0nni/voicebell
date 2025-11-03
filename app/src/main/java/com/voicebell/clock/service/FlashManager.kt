package com.voicebell.clock.service

import android.content.Context
import android.hardware.camera2.CameraManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for controlling device flashlight/LED for alarm notifications.
 *
 * Uses CameraManager to control the torch mode.
 * Flashes in a pattern: 1000ms on, 500ms off.
 */
@Singleton
class FlashManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraId: String? = null
    private var flashJob: Job? = null

    companion object {
        private const val TAG = "FlashManager"
        private const val FLASH_ON_DURATION_MS = 1000L
        private const val FLASH_OFF_DURATION_MS = 500L
    }

    init {
        // Get the first camera with flash capability
        try {
            cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val hasFlash = characteristics.get(
                    android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE
                )
                hasFlash == true
            }

            if (cameraId == null) {
                Log.w(TAG, "No camera with flash found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing flash", e)
            cameraId = null
        }
    }

    /**
     * Starts flashing the LED in a pattern.
     * Pattern: 1000ms on, 500ms off, repeating.
     *
     * @param scope CoroutineScope to launch the flash job in
     */
    fun startFlashing(scope: CoroutineScope) {
        if (cameraId == null) {
            Log.w(TAG, "Cannot start flashing - no camera with flash")
            return
        }

        // Stop any existing flash
        stopFlashing()

        flashJob = scope.launch {
            try {
                while (isActive) {
                    // Turn flash on
                    setTorchMode(true)
                    delay(FLASH_ON_DURATION_MS)

                    // Turn flash off
                    setTorchMode(false)
                    delay(FLASH_OFF_DURATION_MS)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during flashing", e)
                // Ensure flash is off if error occurs
                setTorchMode(false)
            }
        }
    }

    /**
     * Stops flashing and turns off the LED.
     */
    fun stopFlashing() {
        flashJob?.cancel()
        flashJob = null
        setTorchMode(false)
    }

    /**
     * Sets the torch mode on or off.
     */
    private fun setTorchMode(enabled: Boolean) {
        if (cameraId == null) return

        try {
            cameraManager.setTorchMode(cameraId!!, enabled)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting torch mode to $enabled", e)
        }
    }

    /**
     * Checks if the device has a flash.
     */
    fun hasFlash(): Boolean = cameraId != null

    /**
     * Releases resources. Should be called when no longer needed.
     */
    fun release() {
        stopFlashing()
    }
}
