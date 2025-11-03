# VoiceBell Architecture Documentation

This document describes the technical architecture of VoiceBell, a privacy-focused Android alarm clock application.

## Table of Contents

- [Overview](#overview)
- [Architecture Pattern](#architecture-pattern)
- [Layer Details](#layer-details)
- [Technology Stack](#technology-stack)
- [Module Structure](#module-structure)
- [Data Flow](#data-flow)
- [Key Components](#key-components)
- [Design Patterns](#design-patterns)

## Overview

VoiceBell follows **Clean Architecture** principles combined with **MVI (Model-View-Intent)** pattern for the presentation layer. This approach provides:

- **Separation of concerns**: Each layer has a single responsibility
- **Testability**: Business logic is independent of Android framework
- **Maintainability**: Clear boundaries between components
- **Scalability**: Easy to add new features without affecting existing code

## Architecture Pattern

### Three-Layer Architecture

```
┌─────────────────────────────────────────────────────────┐
│                  Presentation Layer                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │   Compose    │  │  ViewModels  │  │    Theme     │ │
│  │     UI       │←→│   (MVI)      │  │  Components  │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
└──────────────────────────┬──────────────────────────────┘
                           │ State/Events
┌──────────────────────────▼──────────────────────────────┐
│                    Domain Layer                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │  Use Cases   │  │    Models    │  │ Repository   │ │
│  │  (Business)  │  │  (Entities)  │  │  Interfaces  │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
└──────────────────────────┬──────────────────────────────┘
                           │ Data Operations
┌──────────────────────────▼──────────────────────────────┐
│                     Data Layer                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │ Repositories │  │  Room DB     │  │  DataStore   │ │
│  │    (Impl)    │  │  (Local)     │  │  (Prefs)     │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────┘
```

## Layer Details

### 1. Presentation Layer

**Responsibility**: UI rendering and user interaction

**Components**:
- **Composables**: Jetpack Compose UI functions
- **ViewModels**: State management and event handling (MVI)
- **UI State**: Immutable data classes representing screen state
- **Events**: Sealed classes representing user actions
- **Effects**: One-time events (navigation, toasts, etc.)

**Package**: `com.voicebell.clock.presentation`

**Example Structure**:
```kotlin
// State (Model)
data class AlarmScreenState(
    val alarms: List<Alarm> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// Events (Intent)
sealed class AlarmEvent {
    data class SetAlarm(val alarm: Alarm) : AlarmEvent()
    data class DeleteAlarm(val id: Long) : AlarmEvent()
    data class ToggleAlarm(val id: Long) : AlarmEvent()
}

// Effects (Side Effects)
sealed class AlarmEffect {
    data class ShowToast(val message: String) : AlarmEffect()
    object NavigateToSettings : AlarmEffect()
}

// ViewModel
@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val getAlarmsUseCase: GetAlarmsUseCase,
    private val createAlarmUseCase: CreateAlarmUseCase,
    private val deleteAlarmUseCase: DeleteAlarmUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AlarmScreenState())
    val state: StateFlow<AlarmScreenState> = _state.asStateFlow()

    private val _effect = Channel<AlarmEffect>()
    val effect: Flow<AlarmEffect> = _effect.receiveAsFlow()

    init {
        loadAlarms()
    }

    fun onEvent(event: AlarmEvent) {
        when (event) {
            is AlarmEvent.SetAlarm -> setAlarm(event.alarm)
            is AlarmEvent.DeleteAlarm -> deleteAlarm(event.id)
            is AlarmEvent.ToggleAlarm -> toggleAlarm(event.id)
        }
    }
}

// View (Composable)
@Composable
fun AlarmScreen(
    viewModel: AlarmViewModel = hiltViewModel(),
    navigateToSettings: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AlarmEffect.ShowToast -> { /* show toast */ }
                AlarmEffect.NavigateToSettings -> navigateToSettings()
            }
        }
    }

    AlarmScreenContent(
        state = state,
        onEvent = viewModel::onEvent
    )
}
```

### 2. Domain Layer

**Responsibility**: Business logic and rules

**Components**:
- **Use Cases**: Single-purpose business operations
- **Domain Models**: Core entities (Alarm, Timer, etc.)
- **Repository Interfaces**: Contracts for data access

**Package**: `com.voicebell.clock.domain`

**Characteristics**:
- Pure Kotlin (no Android dependencies)
- Framework-independent
- Highly testable

**Example**:
```kotlin
// Domain Model
data class Alarm(
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val label: String = "",
    val repeatDays: Set<DayOfWeek> = emptySet(),
    val alarmTone: AlarmTone = AlarmTone.Default,
    val vibrate: Boolean = true,
    val flash: Boolean = false,
    val gradualVolumeIncrease: Boolean = true,
    val snoozeEnabled: Boolean = true,
    val snoozeDuration: Int = 10,
    val preAlarmCount: Int = 0,
    val preAlarmInterval: Int = 7
) {
    fun getNextTriggerTime(): Long {
        // Business logic to calculate next trigger
    }
}

// Repository Interface
interface AlarmRepository {
    fun getAlarms(): Flow<List<Alarm>>
    suspend fun getAlarmById(id: Long): Alarm?
    suspend fun createAlarm(alarm: Alarm): Long
    suspend fun updateAlarm(alarm: Alarm)
    suspend fun deleteAlarm(id: Long)
}

// Use Case
class CreateAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(alarm: Alarm): Result<Long> {
        return try {
            val id = repository.createAlarm(alarm)
            if (alarm.isEnabled) {
                alarmScheduler.scheduleAlarm(alarm.copy(id = id))
            }
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 3. Data Layer

**Responsibility**: Data persistence and retrieval

**Components**:
- **Repository Implementations**: Concrete implementations of repository interfaces
- **Data Sources**: Room database, DataStore
- **DAOs**: Database access objects
- **Entities**: Database table representations

**Package**: `com.voicebell.clock.data`

**Example**:
```kotlin
// Room Entity
@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean,
    val label: String,
    val repeatDays: String, // Serialized set
    val alarmTone: String,
    val vibrate: Boolean,
    val flash: Boolean,
    val gradualVolumeIncrease: Boolean,
    val snoozeEnabled: Boolean,
    val snoozeDuration: Int,
    val preAlarmCount: Int,
    val preAlarmInterval: Int
)

// DAO
@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY hour, minute")
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Long): AlarmEntity?

    @Insert
    suspend fun insert(alarm: AlarmEntity): Long

    @Update
    suspend fun update(alarm: AlarmEntity)

    @Delete
    suspend fun delete(alarm: AlarmEntity)
}

// Repository Implementation
class AlarmRepositoryImpl @Inject constructor(
    private val alarmDao: AlarmDao
) : AlarmRepository {

    override fun getAlarms(): Flow<List<Alarm>> {
        return alarmDao.getAllAlarms()
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun createAlarm(alarm: Alarm): Long {
        return alarmDao.insert(alarm.toEntity())
    }

    // ... other methods
}

// Mappers
fun AlarmEntity.toDomain(): Alarm = Alarm(
    id = id,
    hour = hour,
    minute = minute,
    // ... map all fields
)

fun Alarm.toEntity(): AlarmEntity = AlarmEntity(
    id = if (id == 0L) 0 else id,
    hour = hour,
    minute = minute,
    // ... map all fields
)
```

## Technology Stack

### Core Technologies

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Kotlin | 1.9.22 |
| UI Framework | Jetpack Compose | BOM 2024.02.00 |
| Design System | Material Design 3 | Latest |
| Database | Room | 2.6.1 |
| DI Framework | Hilt | 2.50 |
| Async | Coroutines + Flow | 1.7.3 |
| Navigation | Navigation Compose | 2.7.7 |
| Background Tasks | WorkManager | 2.9.0 |
| Voice Recognition | Vosk | 0.3.45 |
| Preferences | DataStore | 1.0.0 |

### Architecture Components

- **ViewModel**: Lifecycle-aware state management
- **LiveData/StateFlow**: Observable data holders
- **Room**: SQLite abstraction layer
- **WorkManager**: Deferrable background tasks
- **Navigation**: Type-safe navigation

## Module Structure

### Package Organization

```
com.voicebell.clock/
│
├── di/                              # Dependency Injection
│   ├── AppModule.kt                # App-level dependencies
│   ├── DatabaseModule.kt           # Room database
│   ├── RepositoryModule.kt         # Repository bindings
│   └── VoiceModule.kt              # Vosk initialization
│
├── data/
│   ├── local/
│   │   ├── database/
│   │   │   ├── ClockDatabase.kt    # Room database
│   │   │   ├── dao/
│   │   │   │   ├── AlarmDao.kt
│   │   │   │   ├── TimerDao.kt
│   │   │   │   ├── StopwatchDao.kt
│   │   │   │   ├── WorldClockDao.kt
│   │   │   │   └── SettingsDao.kt
│   │   │   └── entities/
│   │   │       ├── AlarmEntity.kt
│   │   │       ├── TimerEntity.kt
│   │   │       ├── WorldClockEntity.kt
│   │   │       └── SettingsEntity.kt
│   │
│   ├── mapper/
│   │   ├── AlarmMapper.kt          # Entity ↔ Domain
│   │   ├── TimerMapper.kt
│   │   └── SettingsMapper.kt
│   │
│   └── repository/
│       ├── AlarmRepositoryImpl.kt
│       ├── TimerRepositoryImpl.kt
│       ├── WorldClockRepositoryImpl.kt
│       └── SettingsRepositoryImpl.kt
│
├── domain/
│   ├── model/
│   │   ├── Alarm.kt
│   │   ├── Timer.kt
│   │   ├── Stopwatch.kt
│   │   ├── WorldClock.kt
│   │   ├── AlarmTone.kt
│   │   ├── Settings.kt
│   │   ├── UiMode.kt              # Classic / Experimental
│   │   └── ThemeMode.kt           # Light / Dark / System
│   │
│   ├── repository/
│   │   ├── AlarmRepository.kt      # Interfaces
│   │   ├── TimerRepository.kt
│   │   ├── WorldClockRepository.kt
│   │   └── SettingsRepository.kt
│   │
│   └── usecase/
│       ├── alarm/
│       │   ├── CreateAlarmUseCase.kt
│       │   ├── UpdateAlarmUseCase.kt
│       │   ├── DeleteAlarmUseCase.kt
│       │   ├── GetAlarmsUseCase.kt
│       │   └── ScheduleAlarmUseCase.kt
│       ├── timer/
│       │   ├── StartTimerUseCase.kt
│       │   └── StopTimerUseCase.kt
│       ├── settings/
│       │   ├── GetSettingsUseCase.kt
│       │   └── UpdateUiModeUseCase.kt
│       └── voice/
│           ├── RecognizeVoiceCommandUseCase.kt
│           └── ParseCommandUseCase.kt
│
├── presentation/
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   │
│   ├── components/
│   │   ├── AlarmCard.kt
│   │   ├── TimePicker.kt
│   │   ├── VoiceButton.kt
│   │   └── DaySelector.kt
│   │
│   ├── navigation/
│   │   ├── NavGraph.kt
│   │   └── Screen.kt
│   │
│   └── screens/
│       ├── home/
│       │   ├── MainScreen.kt          # UI mode switcher
│       │   ├── MainViewModel.kt
│       │   ├── MainState.kt
│       │   ├── ClassicHomeScreen.kt   # Tab layout
│       │   └── ExperimentalHomeScreen.kt # All-in-one
│       ├── alarm/
│       │   ├── AlarmScreen.kt
│       │   ├── AlarmViewModel.kt
│       │   ├── AlarmState.kt
│       │   ├── AlarmEvent.kt
│       │   ├── AlarmEffect.kt
│       │   └── AlarmCard.kt
│       ├── timer/
│       ├── stopwatch/
│       ├── worldclock/
│       └── settings/
│           ├── SettingsScreen.kt
│           ├── SettingsViewModel.kt
│           └── SettingsState.kt
│
├── service/
│   ├── AlarmService.kt              # Foreground service
│   ├── AlarmScheduler.kt            # AlarmManager wrapper
│   └── VoiceRecognitionService.kt   # Vosk service
│
├── receiver/
│   ├── AlarmReceiver.kt             # Handle alarm triggers
│   └── BootReceiver.kt              # Reschedule after reboot
│
├── worker/
│   ├── TimerWorker.kt
│   ├── StopwatchWorker.kt
│   └── RescheduleAlarmsWorker.kt
│
├── util/
│   ├── Constants.kt
│   ├── TimeFormatter.kt
│   ├── VolumeController.kt
│   ├── AlarmSoundPlayer.kt
│   └── Extensions.kt
│
├── VoiceBellApplication.kt          # Application class
└── MainActivity.kt                  # Single activity
```

## Data Flow

### MVI Data Flow

```
┌─────────────┐
│    User     │
│  Interaction│
└──────┬──────┘
       │
       ▼
┌─────────────┐      ┌──────────────┐
│   Event     │─────▶│  ViewModel   │
│  (Intent)   │      │   onEvent()  │
└─────────────┘      └──────┬───────┘
                            │
                            ▼
                     ┌──────────────┐
                     │  Use Case    │
                     │  (Domain)    │
                     └──────┬───────┘
                            │
                            ▼
                     ┌──────────────┐
                     │  Repository  │
                     │   (Data)     │
                     └──────┬───────┘
                            │
                            ▼
                     ┌──────────────┐
                     │  Database    │
                     └──────┬───────┘
                            │
       ┌────────────────────┘
       │
       ▼
┌──────────────┐     ┌──────────────┐
│  StateFlow   │────▶│  Composable  │
│   (State)    │     │     (View)   │
└──────────────┘     └──────────────┘
                            │
                            ▼
                     ┌──────────────┐
                     │   Rendered   │
                     │      UI      │
                     └──────────────┘
```

### Alarm Scheduling Flow

```
User taps "Save Alarm"
       │
       ▼
AlarmEvent.SetAlarm
       │
       ▼
AlarmViewModel.onEvent()
       │
       ▼
CreateAlarmUseCase
       │
       ├─▶ AlarmRepository.createAlarm() ─▶ Room Database
       │
       └─▶ AlarmScheduler.scheduleAlarm()
                  │
                  ▼
           AlarmManager.setAlarmClock()
                  │
                  ▼
           System schedules PendingIntent
                  │
                  ▼ (at trigger time)
           AlarmReceiver.onReceive()
                  │
                  ▼
           AlarmService starts (foreground)
                  │
                  ▼
           Show full-screen activity
           Play alarm sound
```

## Key Components

### 1. UI Modes (Classic & Experimental)

VoiceBell provides two distinct UI layouts that users can switch between:

**Classic Mode (Default)**
- Traditional bottom tab navigation
- Separate dedicated screens for:
  - Alarms
  - World Clocks
  - Timer
  - Stopwatch
- Familiar layout similar to Google Clock
- Best for users who prefer organized, focused interfaces

**Experimental Mode**
- All-in-one screen design
- Recent alarms section at top (up to 3)
  - Quick enable/disable toggles
  - Tap to edit
- Recent timers below (up to 3)
  - One-tap restart functionality
  - Tap to edit
- Large, prominent voice command button in center
  - Visual emphasis on voice features
  - Direct access to Vosk voice recognition
- Quick stopwatch launcher at bottom
- Expandable FAB (+) with mini-FABs for:
  - Create new alarm
  - Create new timer
- Optimized for single-screen workflow
- Ideal for voice-first users

**Implementation**

```kotlin
// UiMode enum
enum class UiMode {
    CLASSIC,
    EXPERIMENTAL
}

// Settings entity (Room)
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val uiMode: String = "CLASSIC",
    val voiceCommandEnabled: Boolean = true,
    val maxRecentAlarms: Int = 3,
    val maxRecentTimers: Int = 3
)

// MainScreen switches based on settings
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (state.settings.uiMode) {
        UiMode.CLASSIC -> ClassicHomeScreen(...)
        UiMode.EXPERIMENTAL -> ExperimentalHomeScreen(...)
    }
}
```

**Switching UI Modes**
1. User taps Settings icon
2. Selects "UI Mode"
3. Chooses Classic or Experimental
4. UpdateUiModeUseCase updates settings
5. Room database persists choice
6. MainScreen observes settings Flow
7. UI automatically switches

### 2. Alarm Scheduling

**AlarmScheduler.kt**
```kotlin
class AlarmScheduler @Inject constructor(
    private val context: Context,
    private val alarmManager: AlarmManager
) {
    fun scheduleAlarm(alarm: Alarm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            require(alarmManager.canScheduleExactAlarms()) {
                "Cannot schedule exact alarms"
            }
        }

        val triggerTime = alarm.getNextTriggerTime()

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarm.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmClockInfo = AlarmManager.AlarmClockInfo(
            triggerTime,
            pendingIntent
        )

        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)

        // Schedule pre-alarms if configured
        if (alarm.preAlarmCount > 0) {
            schedulePreAlarms(alarm)
        }
    }
}
```

### 2. Voice Recognition

**VoiceRecognitionService.kt**
```kotlin
class VoiceRecognitionService @Inject constructor(
    private val context: Context
) {
    private var model: Model? = null
    private var recognizer: Recognizer? = null

    fun initialize() {
        val modelPath = "${context.filesDir}/models/vosk-model-small-en-us-0.15"
        model = Model(modelPath)
        recognizer = Recognizer(model, 16000f)
    }

    fun recognizeFromMicrophone(
        onPartialResult: (String) -> Unit,
        onFinalResult: (String) -> Unit
    ) {
        // Start recording from microphone
        // Process audio with Vosk
        // Return recognized text
    }

    fun shutdown() {
        recognizer?.close()
        model?.close()
    }
}
```

### 3. Database

**ClockDatabase.kt**
```kotlin
@Database(
    entities = [
        AlarmEntity::class,
        TimerEntity::class,
        WorldClockEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class ClockDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun timerDao(): TimerDao
    abstract fun worldClockDao(): WorldClockDao
}
```

## Design Patterns

### 1. Repository Pattern
- Abstracts data sources from business logic
- Provides clean API for data access
- Enables easy testing with mock repositories

### 2. Use Case Pattern (Interactor)
- Encapsulates single business operation
- Reusable across multiple ViewModels
- Testable in isolation

### 3. Observer Pattern
- Flow/StateFlow for reactive data streams
- UI automatically updates when data changes
- Lifecycle-aware with `collectAsStateWithLifecycle()`

### 4. Dependency Injection
- Hilt for compile-time DI
- Constructor injection preferred
- Easy mocking for tests

### 5. State Pattern
- Sealed classes for events and effects
- Immutable state objects
- Predictable state transitions

## Testing Strategy

### Unit Tests (80%)
- ViewModels: State transitions, event handling
- Use Cases: Business logic
- Utilities: Formatters, parsers
- Repository: Data transformations

### Integration Tests (15%)
- Repository + DAO
- Use Case + Repository
- AlarmScheduler + AlarmManager

### UI Tests (5%)
- Critical user flows
- Alarm creation/deletion
- Voice command interaction
- Navigation

## Performance Considerations

1. **Database**
   - Use Flow for reactive queries
   - Index frequently queried columns
   - Batch operations when possible

2. **UI**
   - Compose recomposition optimization
   - Remember expensive calculations
   - Lazy lists for large datasets

3. **Background**
   - WorkManager for deferrable tasks
   - Foreground service for alarms
   - Minimize wake locks

4. **Voice Recognition**
   - Load model only when needed
   - Release resources promptly
   - Handle errors gracefully

## Security & Privacy

1. **No Network Access**
   - No internet permission in manifest
   - All data processing is local

2. **Data Storage**
   - Room database with SQLCipher (future)
   - Encrypted preferences for sensitive data

3. **Permissions**
   - Runtime permission requests
   - Clear rationale for each permission

4. **Voice Data**
   - Never leaves device
   - Vosk processes locally
   - No cloud upload

---

This architecture ensures VoiceBell remains maintainable, testable, and privacy-focused while providing a great user experience.
