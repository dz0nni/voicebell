# VoiceBell

> **⚠️ ACTIVE DEVELOPMENT WARNING**
> This project is currently under intensive development. Many features described in this README are not yet fully functional or may not work as expected. The app is in early alpha stage and should be considered experimental. Use at your own risk.

A privacy-focused, offline alarm clock for Android with voice command support.

## Try it Out

Want to test the current alpha version? Download the latest APK:

**[📥 Download Latest APK](https://github.com/dz0nni/voicebell/releases/latest)**

## Features

### Voice Commands (100% Offline)
Control your alarms and timers using natural English voice commands:
- "Set alarm for 7 AM"
- "Set timer for 5 minutes"
- "Set alarm for 8:30 in the morning"
- "Set timer for 10 minutes"

All voice processing happens 100% on-device using [Vosk](https://alphacephei.com/vosk/) - no internet required.

See [VOICE_COMMANDS.md](VOICE_COMMANDS.md) for complete list.

### Core Functionality
- **Alarms** with full customization
  - Repeating alarms with weekday selection
  - Gradual volume increase for gentle wake-up
  - Snooze functionality
  - Pre-alarms (1-10 configurable pre-alarms before main alarm)
  - Multiple alarm tones
  - Per-alarm vibration and flash options
  - 3-tier fallback system (prevents silent alarms)
  - Respects system alarm volume settings

- **Timer** with voice and manual controls

- **Stopwatch** with lap tracking

### Privacy First
- **No Internet permission** - works 100% offline
- **No Google Play Services** - works on degoogled devices
- **No analytics or tracking** - your data stays on your device
- **Open source** - auditable code under GPL-3.0

### Modern Design
- Material Design 3 (Material You) with dynamic theming
- **All-in-one screen** with:
  - Recent alarms at top (quick enable/disable)
  - Recent timers with restart buttons
  - Large voice command button
  - Quick stopwatch access
  - Expandable FAB for quick alarm/timer creation
- Adaptive layouts for phones and tablets
- Dark mode support
- Clean, intuitive interface

## Download

### F-Droid
[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid" height="80">](https://f-droid.org/packages/com.voicebell.clock)

### GitHub Releases
Download the latest APK from [Releases](https://github.com/dz0nni/voicebell/releases)

### Obtainium
Add this repository URL to Obtainium for automatic updates:
```
https://github.com/dz0nni/voicebell
```

### Google Play
[<img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" alt="Get it on Google Play" height="80">](https://play.google.com/store/apps/details?id=com.voicebell.clock)

## Requirements

- Android 10.0 (API 29) or higher
- Microphone (for voice commands)
- ~60MB storage (includes offline voice recognition model)

## Installation

### From APK
1. Download the APK from [Releases](https://github.com/dz0nni/voicebell/releases)
2. Enable "Install from Unknown Sources" in Android settings
3. Open the APK file and install

### From Source
See [BUILD.md](docs/BUILD.md) for detailed build instructions.

```bash
git clone https://github.com/dz0nni/voicebell.git
cd voicebell
./gradlew assembleRelease
```

## Usage

### Setting an Alarm
**Using Voice Commands (Recommended):**
1. Tap the large microphone button on the main screen
2. Speak your command: "Set alarm for 7 AM"
3. Voice recognition confirms your alarm

**Manual Setup:**
1. Tap the **+** button (FAB) on the main screen
2. Select "Alarm"
3. Set your desired time
4. Choose repeat days (optional)
5. Customize alarm tone, vibration, flash, and other options
6. Tap **Save**

### Using Pre-Alarms
For deep sleepers, enable pre-alarms to receive gentle notifications before your main alarm:
1. When creating/editing an alarm, tap "Pre-alarms"
2. Choose number of pre-alarms (1-10)
3. Set interval between pre-alarms (default: 7 minutes)
4. Pre-alarms will start at `(count × interval)` minutes before main alarm

### Setting a Timer
**Using Voice Commands (Recommended):**
1. Tap the large microphone button on the main screen
2. Speak your command: "Set timer for 5 minutes"
3. Voice recognition confirms your timer

**Manual Setup:**
1. Tap the **+** button (FAB) on the main screen
2. Select "Timer"
3. Enter duration using the picker
4. Tap **Start**

### Voice Commands
1. Grant microphone permission when prompted (Settings → Permissions)
2. Download voice recognition model (Settings → Voice Commands → Setup)
3. Tap the large microphone button on the main screen
4. Speak your command clearly in English
5. Voice recognition works completely offline

### App Settings
Access settings by tapping the ⚙️ icon in the top right corner:

**Voice Commands:**
- Enable/disable voice commands
- Download offline voice recognition model (~40 MB)
- Delete voice model to free up storage

**Display:**
- Toggle 24-hour format

**Permissions:** (all required for full functionality)
- Microphone Access (Critical - for voice commands)
- Notifications (Critical - for alarm/timer alerts)
- Schedule Exact Alarms (Critical - for precise alarm timing)
- Battery Optimization (Important - for reliable background operation)
- Full-Screen Notifications (Recommended - show alarms when phone is locked)

**About:**
- App version information
- Open source licenses

## Privacy

VoiceBell is designed with privacy as the top priority:

- **No network access**: The app has NO internet permission
- **Offline voice recognition**: Uses [Vosk](https://alphacephei.com/vosk/) for 100% on-device processing
- **No data collection**: We don't collect, store, or transmit any user data
- **No third-party SDKs**: No analytics, crash reporting, or ad frameworks
- **Open source**: Full transparency with GPL-3.0 license

See our [Privacy Policy](PRIVACY_POLICY.md) for details.

## Technology Stack

- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVI (Model-View-Intent) + Clean Architecture
- **Database**: Room (SQLite)
- **Voice Recognition**: Vosk (offline)
- **Dependency Injection**: Hilt
- **Background Tasks**: WorkManager
- **Min SDK**: 29 (Android 10)
- **Target SDK**: 35 (Android 15)

See [ARCHITECTURE.md](ARCHITECTURE.md) for technical details.

## Documentation

VoiceBell has comprehensive documentation for users, contributors, and developers:

- **[Documentation Index](DOCUMENTATION_INDEX.md)** - Complete overview of all project documentation
- **[Architecture](ARCHITECTURE.md)** - Technical architecture and design decisions
- **[Testing Guide](UNIT_TESTING.md)** - Comprehensive unit testing guide
- **[Testing Summary](TESTING_SUMMARY.md)** - Current testing status and statistics
- **[Voice Commands](VOICE_COMMANDS.md)** - Complete voice command reference
- **[Contributing](CONTRIBUTING.md)** - Contribution guidelines
- **[Privacy Policy](PRIVACY_POLICY.md)** - Privacy and data handling
- **[Build Instructions](docs/BUILD.md)** - How to build from source

For a complete list of all documentation, see [DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md).

## Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

### Quick Start for Developers
1. Fork the repository
2. Clone your fork
3. Create a feature branch: `git checkout -b feature/amazing-feature`
4. Make your changes
5. Run tests: `./gradlew test`
6. Commit: `git commit -m "Add amazing feature"`
7. Push: `git push origin feature/amazing-feature`
8. Open a Pull Request

## Building

See [docs/BUILD.md](docs/BUILD.md) for detailed build instructions.

**Quick build:**
```bash
./gradlew assembleDebug
```

**Release build:**
```bash
./gradlew assembleRelease
```

## Testing

```bash
# Run unit tests
./gradlew test

# Run instrumentation tests
./gradlew connectedAndroidTest

# Run all tests
./gradlew check
```

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

```
Copyright (C) 2025 VoiceBell Contributors

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
```

## Acknowledgments

- [Vosk](https://alphacephei.com/vosk/) - Offline speech recognition
- [Material Design 3](https://m3.material.io/) - Design system
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern Android UI

## Support

- **Bug reports**: [GitHub Issues](https://github.com/dz0nni/voicebell/issues)
- **Feature requests**: [GitHub Discussions](https://github.com/dz0nni/voicebell/discussions)
- **Security issues**: See [SECURITY.md](SECURITY.md)

## Roadmap

- [x] Basic alarm functionality
- [x] Voice command support (offline)
- [x] Timer and stopwatch
- [x] 3-tier alarm fallback system (prevents silent alarms)
- [ ] World clocks for multiple time zones
- [ ] Alarm challenges (math problems, shake to dismiss)
- [ ] Custom alarm sounds from storage
- [ ] Sleep timer
- [ ] Bedtime routine
- [ ] More language support for voice commands

## FAQ

### Does this work on devices without Google Play Services?
Yes! VoiceBell is specifically designed to work on degoogled devices, LineageOS, GrapheneOS, etc.

### How accurate is the offline voice recognition?
Vosk provides good accuracy for English commands in quiet environments. For best results, speak clearly and use the exact command phrases listed in VOICE_COMMANDS.md.

### Why is the APK so large?
The app includes a ~40MB offline voice recognition model to enable voice commands without internet access.

### Can I use my own alarm sounds?
This feature is planned for a future release. Currently, you can choose from the built-in alarm tones.

### Does this drain battery?
No. The app only uses background resources when alarms are scheduled (using Android's efficient AlarmManager). Voice recognition only runs when you actively press the microphone button.

---

Made with privacy and respect for users.
