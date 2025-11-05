package com.voicebell.clock.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
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
     * Check if USE_FULL_SCREEN_INTENT permission is granted (Android 14+)
     */
    fun canUseFullScreenIntent(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Use NotificationManagerCompat to check full screen intent capability
            NotificationManagerCompat.from(context).canUseFullScreenIntent()
        } else {
            true // Not needed on older versions
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
     * Open full-screen intent settings (Android 14+)
     * Opens the specific settings page for "Alarms & Reminders" notification category
     */
    fun openFullScreenIntentSettings() {
        try {
            val intent = Intent().apply {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                        // Android 14+: Open notification settings and let user navigate to full-screen intent
                        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
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
        } catch (e: Exception) {
            // Fallback to general app settings
            openNotificationSettings()
        }
    }

    /**
     * Open battery optimization settings
     * Opens the correct settings page for user to disable battery optimization
     */
    fun openBatteryOptimizationSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                // Primary: Try to open app-specific battery settings page
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback: Open general battery optimization list
                try {
                    val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(fallbackIntent)
                } catch (e2: Exception) {
                    // Last resort: Open general settings
                    val settingsIntent = Intent(Settings.ACTION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(settingsIntent)
                }
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
     * Get a summary of all permission statuses
     */
    data class PermissionStatus(
        val notificationsEnabled: Boolean,
        val canScheduleExactAlarms: Boolean,
        val canUseFullScreenIntent: Boolean,
        val batteryOptimizationDisabled: Boolean
    ) {
        val allGranted: Boolean
            get() = notificationsEnabled && canScheduleExactAlarms &&
                   canUseFullScreenIntent && batteryOptimizationDisabled

        val criticalGranted: Boolean
            get() = notificationsEnabled && canScheduleExactAlarms
    }

    /**
     * Get current permission status
     */
    fun getPermissionStatus(): PermissionStatus {
        return PermissionStatus(
            notificationsEnabled = areNotificationsEnabled(),
            canScheduleExactAlarms = canScheduleExactAlarms(),
            canUseFullScreenIntent = canUseFullScreenIntent(),
            batteryOptimizationDisabled = isBatteryOptimizationDisabled()
        )
    }
}
