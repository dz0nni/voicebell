# VoiceBell - Next Steps

This file tracks the development roadmap and next implementation steps.

## Project Status: Initial Setup Complete ✅

The project structure, build configuration, and comprehensive documentation are now in place.

## Completed ✅

- [x] Project structure and directory organization
- [x] Gradle build configuration (Kotlin DSL)
- [x] AndroidManifest.xml with all required permissions
- [x] Material Design 3 theme setup
- [x] Hilt dependency injection configuration
- [x] Comprehensive documentation (README, CONTRIBUTING, ARCHITECTURE, etc.)
- [x] GPL-3.0 license
- [x] F-Droid metadata and submission guide
- [x] Build instructions
- [x] Privacy policy
- [x] Security policy
- [x] Voice commands reference
- [x] .gitignore configuration
- [x] Fastlane metadata structure
- [x] Application class and MainActivity placeholders

## Phase 1: Core Infrastructure (Week 1-2)

### Database Layer
- [ ] Create Room database (`ClockDatabase.kt`)
- [ ] Define database entities:
  - [ ] `AlarmEntity.kt` - Alarm data model
  - [ ] `TimerEntity.kt` - Timer data model
  - [ ] `WorldClockEntity.kt` - World clock data model
  - [ ] `StopwatchStateEntity.kt` - Stopwatch persistence
- [ ] Create DAOs:
  - [ ] `AlarmDao.kt` - Alarm database operations
  - [ ] `TimerDao.kt` - Timer database operations
  - [ ] `WorldClockDao.kt` - World clock operations
- [ ] Database migrations setup
- [ ] Write database unit tests

### Domain Layer
- [ ] Create domain models:
  - [ ] `Alarm.kt` - Business logic model
  - [ ] `Timer.kt`
  - [ ] `Stopwatch.kt`
  - [ ] `WorldClock.kt`
  - [ ] `AlarmTone.kt` enum
  - [ ] `DayOfWeek.kt` enum
- [ ] Define repository interfaces:
  - [ ] `AlarmRepository.kt`
  - [ ] `TimerRepository.kt`
  - [ ] `WorldClockRepository.kt`
- [ ] Create use cases:
  - [ ] `CreateAlarmUseCase.kt`
  - [ ] `DeleteAlarmUseCase.kt`
  - [ ] `UpdateAlarmUseCase.kt`
  - [ ] `GetAlarmsUseCase.kt`
  - [ ] `ToggleAlarmUseCase.kt`

### Data Layer
- [ ] Implement repositories:
  - [ ] `AlarmRepositoryImpl.kt`
  - [ ] `TimerRepositoryImpl.kt`
  - [ ] `WorldClockRepositoryImpl.kt`
- [ ] Create mappers (Entity ↔ Domain):
  - [ ] `AlarmMapper.kt`
  - [ ] `TimerMapper.kt`
- [ ] DataStore preferences setup
- [ ] Write repository tests

### Dependency Injection
- [ ] `DatabaseModule.kt` - Room database injection
- [ ] `RepositoryModule.kt` - Repository bindings
- [ ] `AppModule.kt` - App-level dependencies

## Phase 2: Alarm Core Features (Week 3-4)

### AlarmManager Integration
- [ ] Create `AlarmScheduler.kt` - Wrapper around AlarmManager
- [ ] Implement `ScheduleAlarmUseCase.kt`
- [ ] Handle permission checks (USE_EXACT_ALARM)
- [ ] Test alarm scheduling across Android versions

### Alarm Receiver & Service
- [ ] Create `AlarmReceiver.kt` - BroadcastReceiver for alarms
- [ ] Create `AlarmService.kt` - Foreground service for ringing
- [ ] Implement `BootReceiver.kt` - Reschedule after reboot
- [ ] Handle alarm persistence across reboots

### Alarm UI (Jetpack Compose)
- [ ] Create alarm list screen:
  - [ ] `AlarmScreen.kt` - Main alarm screen
  - [ ] `AlarmViewModel.kt` - State management (MVI)
  - [ ] `AlarmState.kt` - UI state model
  - [ ] `AlarmEvent.kt` - User events
  - [ ] `AlarmEffect.kt` - Side effects
- [ ] Create alarm creation/edit dialog
- [ ] Create alarm card component
- [ ] Implement swipe-to-delete
- [ ] Add empty state

### Alarm Ringing Activity
- [ ] Full-screen alarm activity
- [ ] Dismiss button
- [ ] Snooze button
- [ ] Alarm sound playback
- [ ] Vibration pattern
- [ ] Flash (camera LED) support

## Phase 3: Advanced Alarm Features (Week 5-6)

### Repeating Alarms
- [ ] Weekday selection UI component
- [ ] Logic to calculate next alarm time
- [ ] Handle repeat patterns (daily, weekdays, weekends, custom)

### Gradual Volume Increase
- [ ] Implement `AlarmRingtoneManager.kt`
- [ ] Volume fade-in over 60 seconds
- [ ] Configurable fade duration

### Pre-Alarms
- [ ] Pre-alarm scheduling logic
- [ ] UI for configuring pre-alarms count and interval
- [ ] Gentle notification for pre-alarms

### Snooze
- [ ] Snooze functionality
- [ ] Configurable snooze duration
- [ ] Snooze limit (max times)

### Alarm Tones
- [ ] Built-in alarm sounds
- [ ] Sound picker UI
- [ ] Volume control per alarm

## Phase 4: Timer & Stopwatch (Week 7-8)

### Timer
- [ ] Timer data model and database
- [ ] Timer creation UI
- [ ] Countdown logic with WorkManager
- [ ] Timer notification
- [ ] Timer completion alert
- [ ] Multiple simultaneous timers

### Stopwatch
- [ ] Stopwatch state management
- [ ] Start/Stop/Reset functionality
- [ ] Lap tracking
- [ ] Lap list display
- [ ] Persistent stopwatch state

## Phase 5: Voice Recognition (Week 9-10)

### Vosk Integration
- [ ] Download Vosk model setup
- [ ] Model loading and initialization
- [ ] Create `VoiceRecognitionService.kt`
- [ ] Microphone permission handling
- [ ] Real-time audio capture and processing

### Voice Command Parsing
- [ ] Create `VoiceCommandParser.kt`
- [ ] Implement command regex patterns:
  - [ ] "Set alarm for X AM/PM"
  - [ ] "Set timer for X minutes"
  - [ ] "Cancel alarm"
  - [ ] "What alarms are set?"
- [ ] Natural language time parsing
- [ ] Error handling and user feedback

### Voice UI Components
- [ ] Voice button component
- [ ] Listening indicator
- [ ] Voice feedback animations
- [ ] Command confirmation dialog

### Voice Use Cases
- [ ] `RecognizeVoiceCommandUseCase.kt`
- [ ] `ParseVoiceCommandUseCase.kt`
- [ ] `ExecuteVoiceCommandUseCase.kt`

## Phase 6: World Clocks (Week 11)

### World Clock Feature
- [ ] World clock list screen
- [ ] Add/remove time zones
- [ ] Time zone search
- [ ] Current time display for each zone
- [ ] Analog/digital clock widgets

## Phase 7: Settings & Polish (Week 12)

### Settings Screen
- [ ] Theme selection (Light/Dark/System)
- [ ] Voice commands toggle
- [ ] About section
- [ ] Version information
- [ ] Open source licenses screen

### Navigation
- [ ] Bottom navigation bar
- [ ] Navigation graph setup
- [ ] Deep linking support (for system intents)

### Accessibility
- [ ] Content descriptions
- [ ] TalkBack support
- [ ] Large text support
- [ ] High contrast mode

### Tablet Support
- [ ] Responsive layouts
- [ ] Landscape orientation
- [ ] Large screen optimizations

## Phase 8: Testing (Week 13-14)

### Unit Tests
- [ ] ViewModel tests (all screens)
- [ ] Use case tests (all business logic)
- [ ] Repository tests
- [ ] Parser tests (voice commands)
- [ ] Utility tests

### Integration Tests
- [ ] Database tests
- [ ] Repository + DAO tests
- [ ] AlarmManager integration tests

### UI Tests
- [ ] Compose UI tests (critical flows)
- [ ] Alarm creation test
- [ ] Timer test
- [ ] Voice command test

### Manual Testing
- [ ] Test on multiple Android versions (10-15)
- [ ] Test on different manufacturers (Samsung, Pixel, OnePlus)
- [ ] Overnight alarm reliability test
- [ ] Battery drain test
- [ ] Voice accuracy test in various environments

## Phase 9: Release Preparation (Week 15-16)

### Pre-Release Checklist
- [ ] All features implemented and tested
- [ ] No critical bugs
- [ ] Performance optimization
- [ ] APK size optimization
- [ ] ProGuard rules verified
- [ ] Crash-free for 7 days

### Assets
- [ ] App icon (all densities)
- [ ] Feature graphic (1024x500)
- [ ] Screenshots (phone: 1080x1920 or higher)
  - [ ] Alarm list screen
  - [ ] Alarm creation
  - [ ] Timer screen
  - [ ] Settings screen
  - [ ] Voice command in action
- [ ] Screenshots (tablet: 7" and 10")

### Store Listings
- [ ] Google Play Store listing
  - [ ] Description
  - [ ] Screenshots
  - [ ] Feature graphic
  - [ ] Privacy policy URL
- [ ] F-Droid metadata finalization
- [ ] Update README with download badges

### Signing & Building
- [ ] Generate release keystore
- [ ] Configure signing in build.gradle
- [ ] Build signed APK
- [ ] Build App Bundle (AAB) for Play Store
- [ ] Test signed APK on real device

### Distribution
- [ ] Create GitHub release v1.0.0
- [ ] Upload signed APK to GitHub Releases
- [ ] Submit to F-Droid
- [ ] Submit to Google Play Store
- [ ] Announce release on GitHub Discussions

## Future Enhancements (v1.1+)

- [ ] Alarm challenges (math problems, shake to dismiss)
- [ ] Custom alarm sounds from device storage
- [ ] Sleep timer
- [ ] Bedtime routine
- [ ] Sunrise alarm (gradual screen brightness)
- [ ] Multiple language support (UI and voice)
- [ ] Alarm history and statistics
- [ ] Backup/restore alarms
- [ ] Widget support
- [ ] Wear OS companion app
- [ ] SQLCipher database encryption

## Development Commands

### Build
```bash
./gradlew assembleDebug          # Debug APK
./gradlew assembleRelease        # Release APK
./gradlew bundleRelease          # AAB for Play Store
```

### Test
```bash
./gradlew test                   # Unit tests
./gradlew connectedAndroidTest   # Instrumentation tests
./gradlew lint                   # Lint check
```

### Clean
```bash
./gradlew clean                  # Clean build
```

## Notes

- **Voice Model**: Remember to download `vosk-model-small-en-us-0.15` (40MB) to `app/src/main/assets/models/`
- **Testing**: Test alarms overnight to ensure reliability
- **Battery**: Monitor battery usage during development
- **Permissions**: Request runtime permissions gracefully

## Resources

- Architecture: See [ARCHITECTURE.md](ARCHITECTURE.md)
- Build guide: See [docs/BUILD.md](docs/BUILD.md)
- F-Droid guide: See [docs/F-DROID.md](docs/F-DROID.md)
- Voice commands: See [VOICE_COMMANDS.md](VOICE_COMMANDS.md)

---

**Let's build VoiceBell! 🔔**
