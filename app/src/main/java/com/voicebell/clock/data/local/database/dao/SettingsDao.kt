package com.voicebell.clock.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.voicebell.clock.data.local.database.entities.SettingsEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for accessing app settings.
 * Settings table contains only one row (id=1) with all preferences.
 */
@Dao
interface SettingsDao {
    /**
     * Get settings as a Flow for reactive updates
     */
    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettingsFlow(): Flow<SettingsEntity?>

    /**
     * Get settings as a single value
     */
    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun getSettings(): SettingsEntity?

    /**
     * Insert default settings (called on first launch)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: SettingsEntity)

    /**
     * Update settings
     */
    @Update
    suspend fun update(settings: SettingsEntity)

    /**
     * Update UI mode
     */
    @Query("UPDATE settings SET uiMode = :uiMode WHERE id = 1")
    suspend fun updateUiMode(uiMode: String)

    /**
     * Update theme mode
     */
    @Query("UPDATE settings SET themeMode = :themeMode WHERE id = 1")
    suspend fun updateThemeMode(themeMode: String)

    /**
     * Update 24-hour format preference
     */
    @Query("UPDATE settings SET use24HourFormat = :use24Hour WHERE id = 1")
    suspend fun updateUse24HourFormat(use24Hour: Boolean)

    /**
     * Update voice command enabled
     */
    @Query("UPDATE settings SET voiceCommandEnabled = :enabled WHERE id = 1")
    suspend fun updateVoiceCommandEnabled(enabled: Boolean)

    /**
     * Update auto delete finished timer
     */
    @Query("UPDATE settings SET autoDeleteFinishedTimer = :enabled WHERE id = 1")
    suspend fun updateAutoDeleteFinishedTimer(enabled: Boolean)

    /**
     * Update play timer sound only to Bluetooth headphones
     */
    @Query("UPDATE settings SET playTimerSoundOnlyToBluetooth = :enabled WHERE id = 1")
    suspend fun updatePlayTimerSoundOnlyToBluetooth(enabled: Boolean)
}
