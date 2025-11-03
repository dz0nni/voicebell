package com.voicebell.clock

import android.app.Application
import com.voicebell.clock.domain.repository.SettingsRepository
import com.voicebell.clock.util.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main Application class for VoiceBell.
 *
 * This class is annotated with @HiltAndroidApp to enable Hilt dependency injection
 * throughout the application.
 */
@HiltAndroidApp
class VoiceBellApplication : Application() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        // Initialize notification channels (Android 8.0+)
        notificationHelper.createNotificationChannels()

        // Initialize settings with defaults on first launch
        applicationScope.launch {
            settingsRepository.initializeDefaults()
        }

        // TODO: Initialize other app-level components
        // - Voice recognition model loader (Phase 5)
        // - WorkManager configuration (Phase 4)
    }
}
