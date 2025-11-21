package com.voicebell.clock.util

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * Utility class to detect if Bluetooth headphones/headset is connected.
 *
 * Supports Android 12+ (requires BLUETOOTH_CONNECT permission) and older versions.
 */
class BluetoothHeadsetDetector(private val context: Context) {

    companion object {
        private const val TAG = "BluetoothHeadsetDetector"
    }

    /**
     * Check if Bluetooth headphones are currently connected.
     *
     * @return true if Bluetooth audio device is connected, false otherwise
     */
    fun isBluetoothHeadsetConnected(): Boolean {
        // Check permission for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                Log.w(TAG, "BLUETOOTH_CONNECT permission not granted")
                return false
            }
        }

        return try {
            // Check via AudioManager - only returns true if audio is actively routed to Bluetooth
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            // Check if any Bluetooth audio device is connected
            val audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            val hasBluetoothAudio = audioDevices.any { device ->
                device.type == android.media.AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                device.type == android.media.AudioDeviceInfo.TYPE_BLUETOOTH_SCO
            }

            if (hasBluetoothAudio) {
                Log.d(TAG, "Bluetooth audio device actively connected")
                true
            } else {
                Log.d(TAG, "No active Bluetooth audio connection")
                false
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception checking Bluetooth: ${e.message}")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking Bluetooth: ${e.message}")
            false
        }
    }

    /**
     * Check if a Bluetooth device is an audio device (headset, headphones, speaker).
     */
    private fun isAudioDevice(device: BluetoothDevice): Boolean {
        return try {
            val deviceClass = device.bluetoothClass ?: return false
            val majorDeviceClass = deviceClass.majorDeviceClass

            // Check if device is audio/video type
            majorDeviceClass == 0x0400 // AUDIO_VIDEO major device class
        } catch (e: Exception) {
            Log.e(TAG, "Error checking device class: ${e.message}")
            false
        }
    }

    /**
     * Check if the app has BLUETOOTH_CONNECT permission (Android 12+).
     */
    fun hasBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // No runtime permission needed for older versions
            true
        }
    }
}
