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
            // Method 1: Check via AudioManager (most reliable)
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val isBluetoothA2dpOn = audioManager.isBluetoothA2dpOn
            val isBluetoothScoOn = audioManager.isBluetoothScoOn

            if (isBluetoothA2dpOn || isBluetoothScoOn) {
                Log.d(TAG, "Bluetooth audio detected via AudioManager (A2DP: $isBluetoothA2dpOn, SCO: $isBluetoothScoOn)")
                return true
            }

            // Method 2: Check via BluetoothAdapter (fallback)
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled) {
                val connectedDevices = bluetoothAdapter.bondedDevices
                for (device in connectedDevices) {
                    if (isAudioDevice(device)) {
                        Log.d(TAG, "Bluetooth audio device found: ${device.name}")
                        return true
                    }
                }
            }

            Log.d(TAG, "No Bluetooth headset detected")
            false
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
