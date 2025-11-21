package com.voicebell.clock.util

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages critical permissions for alarm/timer functionality.
 *
 * CRITICAL PERMISSIONS (app won't work without):
 * 1. POST_NOTIFICATIONS (Android 13+)
 * 2. SCHEDULE_EXACT_ALARM (Android 12+)
 * 3. USE_FULL_SCREEN_INTENT (Android 14+)
 * 4. Battery Optimization Bypass
 *
 * Without these, alarms/timers may not work on locked screens!
 */
@Singleton
class PermissionsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "PermissionsManager"

        // Shared preferences key to track if we've shown permission dialogs
        private const val PREFS_NAME = "permissions_manager"
        private const val KEY_SHOWN_FULL_SCREEN_DIALOG = "shown_full_screen_dialog"
        private const val KEY_SHOWN_EXACT_ALARM_DIALOG = "shown_exact_alarm_dialog"
        private const val KEY_SHOWN_BATTERY_DIALOG = "shown_battery_dialog"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Check if all critical permissions are granted.
     * Returns true only if ALL permissions are granted.
     */
    fun areAllCriticalPermissionsGranted(): Boolean {
        val notifications = areNotificationsEnabled()
        val exactAlarm = canScheduleExactAlarms()
        val fullScreen = canUseFullScreenIntent()
        val battery = isIgnoringBatteryOptimizations()

        Log.d(TAG, "Permission status:")
        Log.d(TAG, "  Notifications: $notifications")
        Log.d(TAG, "  Exact Alarms: $exactAlarm")
        Log.d(TAG, "  Full Screen: $fullScreen")
        Log.d(TAG, "  Battery Optimization Bypass: $battery")

        return notifications && exactAlarm && fullScreen && battery
    }

    /**
     * Get list of missing permissions with user-friendly descriptions.
     */
    fun getMissingPermissions(): List<MissingPermission> {
        val missing = mutableListOf<MissingPermission>()

        if (!areNotificationsEnabled()) {
            missing.add(
                MissingPermission(
                    name = "Notifications",
                    description = "Required to show alarm/timer notifications",
                    isCritical = true,
                    canRequestRuntime = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
                    settingsAction = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                )
            )
        }

        if (!canScheduleExactAlarms()) {
            missing.add(
                MissingPermission(
                    name = "Exact Alarms",
                    description = "Required for alarms to ring at exact time",
                    isCritical = true,
                    canRequestRuntime = false,
                    settingsAction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    } else {
                        null
                    }
                )
            )
        }

        if (!canUseFullScreenIntent()) {
            missing.add(
                MissingPermission(
                    name = "Full-Screen Alarms",
                    description = "Required to show alarms on locked screen",
                    isCritical = true,
                    canRequestRuntime = false,
                    settingsAction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT
                    } else {
                        null
                    }
                )
            )
        }

        if (!isIgnoringBatteryOptimizations()) {
            missing.add(
                MissingPermission(
                    name = "Battery Optimization Bypass",
                    description = "Prevents Android from stopping alarms in background",
                    isCritical = true,
                    canRequestRuntime = false,
                    settingsAction = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                )
            )
        }

        return missing
    }

    /**
     * Check if notifications are enabled (required for Android 13+).
     */
    fun areNotificationsEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ requires POST_NOTIFICATIONS permission
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED &&
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        } else {
            // Pre-Android 13, notifications are always enabled
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    /**
     * Check if app can schedule exact alarms (Android 12+).
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true // Pre-Android 12, exact alarms always allowed
        }
    }

    /**
     * Check if app can use full-screen intents (Android 14+).
     * This is CRITICAL for showing alarms on locked screens!
     */
    fun canUseFullScreenIntent(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14+: Check runtime permission
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            val canUse = notificationManager.canUseFullScreenIntent()

            Log.d(TAG, "Android 14+ Full-screen intent check: $canUse")
            canUse
        } else {
            // Pre-Android 14: Permission granted in manifest is sufficient
            true
        }
    }

    /**
     * Check if app is ignoring battery optimizations.
     * This prevents Android from killing alarm services in background.
     */
    fun isIgnoringBatteryOptimizations(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true // Pre-Android 6.0, no battery optimization
        }
    }

    /**
     * Open settings to grant missing permission.
     */
    fun openSettingsForPermission(permission: MissingPermission) {
        try {
            val intent = when (permission.settingsAction) {
                Settings.ACTION_APP_NOTIFICATION_SETTINGS -> {
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                }
                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                    } else {
                        null
                    }
                }
                Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                    } else {
                        null
                    }
                }
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS -> {
                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                }
                else -> {
                    // Fallback: Open app settings
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                }
            }

            intent?.let {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(it)
                Log.i(TAG, "Opened settings for permission: ${permission.name}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open settings for permission: ${permission.name}", e)
        }
    }

    /**
     * Mark that we've shown the permission dialog.
     */
    fun markDialogShown(permission: String) {
        prefs.edit().putBoolean(permission, true).apply()
    }

    /**
     * Check if we've already shown the dialog for this permission.
     */
    fun hasShownDialog(permission: String): Boolean {
        return prefs.getBoolean(permission, false)
    }

    /**
     * Reset all dialog flags (for testing).
     */
    fun resetDialogFlags() {
        prefs.edit().clear().apply()
    }
}

/**
 * Represents a missing permission.
 */
data class MissingPermission(
    val name: String,
    val description: String,
    val isCritical: Boolean,
    val canRequestRuntime: Boolean,
    val settingsAction: String?
)
