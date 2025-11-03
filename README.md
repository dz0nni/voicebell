# VoiceBell

A privacy-focused, offline alarm clock for Android with voice command support.

## Features

### Core Functionality
- **Alarms** with full customization
  - Repeating alarms with weekday selection
  - Gradual volume increase for gentle wake-up
  - Snooze functionality
  - Pre-alarms (1-10 configurable pre-alarms before main alarm)
  - Multiple alarm tones
  - Per-alarm vibration and flash options
  - Respects system alarm volume settings

- **Timer** with voice and manual controls

- **Stopwatch** with lap tracking

- **World Clocks** for multiple time zones

### Voice Commands (100% Offline)
Control your alarms and timers using natural English voice commands:
- "Set alarm for 7 AM"
- "Set timer for 5 minutes"
- "Cancel alarm"
- "What alarms are set?"

See [VOICE_COMMANDS.md](VOICE_COMMANDS.md) for complete list.

### Privacy First
- **No Internet permission** - works 100% offline
- **No Google Play Services** - works on degoogled devices
- **No analytics or tracking** - your data stays on your device
- **Open source** - auditable code under GPL-3.0

### Modern Design
- Material Design 3 (Material You) with dynamic theming
- **Two UI modes**: Switch between Classic and Experimental layouts
  - **Classic Mode**: Traditional tab-based navigation (Alarm, Clock, Timer, Stopwatch)
  - **Experimental Mode**: All-in-one screen with:
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
Download the latest APK from [Releases](https://github.com/yourusername/voicebell/releases)

### Obtainium
Add this repository URL to Obtainium for automatic updates:
```
https://github.com/yourusername/voicebell
```

### Google Play
[<img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" alt="Get it on Google Play" height="80">](https://play.google.com/store/apps/details?id=com.voicebell.clock)

## Requirements

- Android 10.0 (API 29) or higher
- Microphone (for voice commands)
- ~60MB storage (includes offline voice recognition model)

## Installation

### From APK
1. Download the APK from [Releases](https://github.com/yourusername/voicebell/releases)
2. Enable "Install from Unknown Sources" in Android settings
3. Open the APK file and install

### From Source
See [BUILD.md](docs/BUILD.md) for detailed build instructions.

```bash
git clone https://github.com/yourusername/voicebell.git
cd voicebell
./gradlew assembleRelease
```

## Usage

### Switching UI Modes
VoiceBell offers two different layouts to match your preference:

**Classic Mode (Default):**
- Traditional tab navigation at bottom
- Separate screens for Alarm, Clock, Timer, and Stopwatch
- Familiar interface for most users

**Experimental Mode:**
- All features on one screen
- Recent alarms and timers at top for quick access
- Large voice command button in center
- Quick stopwatch launcher at bottom
- Expandable FAB (+) for creating alarms/timers

**To switch modes:**
1. Tap the **Settings** icon (⚙️) in top right
2. Select **UI Mode**
3. Choose between Classic or Experimental
4. The interface updates immediately

### Setting an Alarm
1. Tap the **+** button on the Alarm tab
2. Set your desired time
3. Choose repeat days (optional)
4. Customize alarm tone, vibration, flash, and other options
5. Tap **Save**

**Or use voice:** Tap the microphone button and say "Set alarm for 7 AM"

### Using Pre-Alarms
For deep sleepers, enable pre-alarms to receive gentle notifications before your main alarm:
1. When creating/editing an alarm, tap "Pre-alarms"
2. Choose number of pre-alarms (1-10)
3. Set interval between pre-alarms (default: 7 minutes)
4. Pre-alarms will start at `(count × interval)` minutes before main alarm

### Setting a Timer
1. Go to the Timer tab
2. Enter duration using the picker
3. Tap **Start**

**Or use voice:** "Set timer for 5 minutes"

### Voice Commands
1. Grant microphone permission when prompted
2. Tap the microphone button on any screen
3. Speak your command clearly in English
4. Voice recognition works completely offline

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

- **Bug reports**: [GitHub Issues](https://github.com/yourusername/voicebell/issues)
- **Feature requests**: [GitHub Discussions](https://github.com/yourusername/voicebell/discussions)
- **Security issues**: See [SECURITY.md](SECURITY.md)

## Roadmap

- [x] Basic alarm functionality
- [x] Voice command support
- [x] Timer and stopwatch
- [x] World clocks
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
