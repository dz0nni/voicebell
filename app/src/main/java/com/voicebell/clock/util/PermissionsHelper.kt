package com.voicebell.clock.util

import android.Manifest
import android.app.Activity
import android.app.AppOpsManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.voicebell.clock.MainActivity
import com.voicebell.clock.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for checking and requesting Android permissions.
 */
@Singleton
class PermissionsHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Check if POST_NOTIFICATIONS permission is granted (Android 13+)
     */
    fun areNotificationsEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // On older Android versions, notifications are enabled by default
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    /**
     * Check if SCHEDULE_EXACT_ALARM permission is granted (Android 12+)
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true // Not needed on older versions
        }
    }

    /**
     * Check if USE_FULL_SCREEN_INTENT permission is granted (Android 12+)
     *
     * Android 12+ (API 31+) requires runtime permission for full-screen intents.
     * This uses the official NotificationManagerCompat API which is more reliable
     * than AppOpsManager checks.
     */
    fun canUseFullScreenIntent(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+: Use official API
            try {
                NotificationManagerCompat.from(context).canUseFullScreenIntent()
            } catch (e: Exception) {
                // Fallback: assume granted on error
                false
            }
        } else {
            // Android 11 and below: Full-screen intents work by default
            true
        }
    }

    /**
     * Check if battery optimization is disabled (recommended for reliable alarms)
     */
    fun isBatteryOptimizationDisabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true // Not applicable on older versions
        }
    }

    /**
     * Check if RECORD_AUDIO permission is granted (required for voice commands)
     */
    fun isRecordAudioGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if BLUETOOTH_CONNECT permission is granted (Android 12+)
     * Required to detect Bluetooth headset connection
     */
    fun isBluetoothConnectGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not needed on older versions
        }
    }

    /**
     * Open app notification settings
     */
    fun openNotificationSettings() {
        val intent = Intent().apply {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                else -> {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.parse("package:${context.packageName}")
                }
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    /**
     * Create a test notification with full-screen intent to enable the permission toggle
     * This allows Android to recognize that the app uses full-screen intents
     */
    private fun createTestFullScreenNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                // Create a temporary notification channel if needed
                val channelId = "full_screen_test_channel"
                val channel = NotificationChannel(
                    channelId,
                    "Permission Setup",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Temporary channel for setting up full-screen notifications"
                }
                notificationManager.createNotificationChannel(channel)

                // Create pending intent
                val intent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    999,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Create notification with full-screen intent
                val notification = NotificationCompat.Builder(context, channelId)
                    .setContentTitle("Setting up permissions...")
                    .setContentText("This notification will be dismissed automatically")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setAutoCancel(true)
                    .setTimeoutAfter(100) // Auto-dismiss after 100ms
                    .setFullScreenIntent(pendingIntent, true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build()

                // Post and immediately cancel to register the capability
                notificationManager.notify(999, notification)
                // Cancel after a short delay to ensure Android registers it
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    notificationManager.cancel(999)
                }, 200)
            } catch (e: Exception) {
                // Ignore errors - this is best effort
            }
        }
    }

    /**
     * Open full-screen intent settings (Android 14+)
     * Opens the direct settings page for managing full-screen intent permission
     * First creates a test notification to enable the permission toggle
     */
    fun openFullScreenIntentSettings() {
        // First, create a test notification so Android knows we use full-screen intents
        createTestFullScreenNotification()

        // Small delay to ensure notification is registered before opening settings
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            try {
                val intent = Intent().apply {
                    when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                            // Android 14+: Open the dedicated full-screen intent settings page
                            action = Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT
                            data = Uri.parse("package:${context.packageName}")
                        }
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                            // Android 8-13: Open notification settings (best we can do)
                            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        else -> {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.parse("package:${context.packageName}")
                        }
                    }
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback to general notification settings
                openNotificationSettings()
            }
        }, 300) // 300ms delay
    }

    /**
     * Request to ignore battery optimization - shows direct dialog
     * This is much simpler than opening settings
     */
    fun requestIgnoreBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback: Open battery optimization settings list
                openBatteryOptimizationSettings()
            }
        }
    }

    /**
     * Open battery optimization settings (fallback)
     * Opens the correct settings page for user to disable battery optimization
     */
    fun openBatteryOptimizationSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                // Last resort: Open general settings
                val settingsIntent = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(settingsIntent)
            }
        }
    }

    /**
     * Open alarm & reminders settings (Android 12+)
     */
    fun openAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    /**
     * Open app settings page where user can grant microphone permission
     */
    fun openMicrophoneSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    /**
     * Open app settings page where user can grant Bluetooth permission
     */
    fun openBluetoothSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    /**
     * Get a summary of all permission statuses
     */
    data class PermissionStatus(
        val notificationsEnabled: Boolean,
        val canScheduleExactAlarms: Boolean,
        val canUseFullScreenIntent: Boolean,
        val batteryOptimizationDisabled: Boolean,
        val recordAudioGranted: Boolean,
        val bluetoothConnectGranted: Boolean
    ) {
        val allGranted: Boolean
            get() = notificationsEnabled && canScheduleExactAlarms &&
                   canUseFullScreenIntent && batteryOptimizationDisabled &&
                   recordAudioGranted && bluetoothConnectGranted

        val criticalGranted: Boolean
            get() = recordAudioGranted && notificationsEnabled && canScheduleExactAlarms
    }

    /**
     * Get current permission status
     */
    fun getPermissionStatus(): PermissionStatus {
        return PermissionStatus(
            notificationsEnabled = areNotificationsEnabled(),
            canScheduleExactAlarms = canScheduleExactAlarms(),
            canUseFullScreenIntent = canUseFullScreenIntent(),
            batteryOptimizationDisabled = isBatteryOptimizationDisabled(),
            recordAudioGranted = isRecordAudioGranted(),
            bluetoothConnectGranted = isBluetoothConnectGranted()
        )
    }
}
