# Privacy Policy for VoiceBell

**Effective Date**: January 1, 2025

**Last Updated**: January 1, 2025

## Introduction

VoiceBell is a privacy-focused alarm clock application for Android. This Privacy Policy explains how we handle your data and protect your privacy.

**Short version**: We don't collect, store, transmit, or sell any of your personal data. Everything stays on your device.

## Our Privacy Commitment

VoiceBell is designed with privacy as the top priority:

- **No data collection**: We don't collect any personal information
- **No internet access**: The app has NO internet permission
- **No analytics**: We don't track your usage or behavior
- **No third-party SDKs**: No advertising, analytics, or tracking frameworks
- **100% offline**: All features work completely offline
- **Open source**: Code is publicly auditable under GPL-3.0

## Information We DO NOT Collect

VoiceBell does **NOT** collect, store, transmit, or process:

- ❌ Personal information (name, email, phone number)
- ❌ Location data
- ❌ Device information
- ❌ Usage analytics
- ❌ Crash reports
- ❌ Voice recordings
- ❌ Voice recognition data
- ❌ Alarm settings or times
- ❌ Any other user data

## Data Storage (Local Only)

All data remains on your device and is never transmitted:

### What is Stored Locally

VoiceBell stores the following data **only on your device**:

1. **Alarm Settings**
   - Alarm times, labels, and repeat schedules
   - Alarm tone preferences
   - Vibration and flash settings
   - Stored in local SQLite database (Room)

2. **Timer Data**
   - Active timer durations and labels
   - Stored in local database

3. **App Preferences**
   - Theme settings (light/dark mode)
   - Voice command enabled/disabled
   - Stored in Android DataStore (local)

4. **Voice Recognition Model**
   - Offline Vosk speech recognition model (~40MB)
   - Downloaded to device on first use
   - Stored in app's private directory

### How Local Data is Protected

- All data is stored in app-private storage (inaccessible to other apps)
- Android system enforces per-app data isolation
- Data is deleted when you uninstall the app
- Optional Android backup (see below)

## Permissions Explained

VoiceBell requires the following Android permissions:

### Required Permissions

| Permission | Purpose | Privacy Impact |
|------------|---------|----------------|
| `USE_EXACT_ALARM` | Schedule precise alarms | No data collection, granted automatically |
| `RECEIVE_BOOT_COMPLETED` | Reschedule alarms after device restart | No data collection |
| `WAKE_LOCK` | Wake device when alarm triggers | No data collection |
| `VIBRATE` | Vibrate for alarms/timers | No data collection |
| `FOREGROUND_SERVICE` | Play alarm sounds reliably | No data collection |

### Runtime Permissions (User Approval Required)

| Permission | Purpose | When Requested | Can Be Denied |
|------------|---------|----------------|---------------|
| `RECORD_AUDIO` | Voice commands | First use of voice button | Yes - voice features disabled |
| `POST_NOTIFICATIONS` | Show alarm notifications | First alarm creation (Android 13+) | Yes - reduces functionality |
| `CAMERA` (Flash) | Flash light for alarms | When flash enabled | Yes - flash feature disabled |

### Permissions We DO NOT Request

- ❌ `INTERNET` - We have NO internet access
- ❌ `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`
- ❌ `READ_CONTACTS`
- ❌ `READ_SMS`
- ❌ `READ_PHONE_STATE`
- ❌ `ACCESS_NETWORK_STATE`

## Voice Recognition Privacy

### How Voice Commands Work

1. **Tap microphone button** → Microphone activates
2. **Speak command** → Audio captured by microphone
3. **Local processing** → Vosk library processes audio on-device
4. **Command executed** → Alarm/timer created
5. **Audio discarded** → No recording saved

### Voice Privacy Guarantees

- ✅ **100% offline**: Voice processing uses Vosk (offline library)
- ✅ **No recording**: Audio is processed in real-time, never saved
- ✅ **No transmission**: Voice data never leaves your device
- ✅ **No cloud**: No connection to Google, Amazon, or any cloud service
- ✅ **Microphone access only when active**: Only when you press voice button

### Vosk Speech Recognition

- Open source library: [alphacephei.com/vosk](https://alphacephei.com/vosk/)
- Licensed under Apache 2.0
- Runs entirely on-device using CPU
- No network connection required or used

## Android Backup

### Auto Backup (Optional)

Android allows apps to back up data to Google Drive or device-to-device transfer:

**What is backed up:**
- Alarm settings (times, labels, repeat schedules)
- App preferences (theme, settings)

**What is NOT backed up:**
- Voice recognition model (too large)
- Temporary state

**How to disable:**
1. Android Settings → Google → Backup
2. Toggle off "Back up to Google Drive"

**Privacy note**: If you use Android backup, your alarm data may be stored in your Google account's encrypted backup. We have no access to this backup.

## Third-Party Services

VoiceBell uses **NO third-party services**:

- ❌ No analytics (Google Analytics, Firebase, Crashlytics)
- ❌ No advertising networks
- ❌ No social media SDKs
- ❌ No cloud services
- ❌ No telemetry

### Open Source Dependencies

VoiceBell uses the following open-source libraries (all run locally):

- **Vosk** (Apache 2.0) - Offline voice recognition
- **Jetpack Compose** (Apache 2.0) - UI framework
- **Room** (Apache 2.0) - Local database
- **Hilt** (Apache 2.0) - Dependency injection
- **Kotlin Coroutines** (Apache 2.0) - Asynchronous operations

None of these libraries collect or transmit user data.

## Children's Privacy

VoiceBell does not collect any information from anyone, including children under 13. The app is safe for all ages as it has:

- No data collection
- No internet access
- No in-app purchases
- No advertisements
- No user accounts

## Data Security

### How We Protect Your Data

Since VoiceBell doesn't collect or transmit data, there's minimal security risk:

- **App-private storage**: Android isolates app data
- **No network access**: Cannot be intercepted or hacked remotely
- **No accounts**: No passwords to steal
- **Local-only**: Data never leaves your device

### Device Security

You are responsible for device security:

- Use device PIN/password/biometric lock
- Keep Android OS updated
- Install apps only from trusted sources
- Enable device encryption (on by default in Android 10+)

## Your Privacy Rights

### Access Your Data

All your data is already on your device:

- View alarms in the app
- Export via Android backup
- Access database files (requires root)

### Delete Your Data

You can delete your data at any time:

1. **Delete individual alarms**: Swipe to delete in app
2. **Clear all data**: Settings → Apps → VoiceBell → Storage → Clear Data
3. **Complete removal**: Uninstall the app

Uninstalling VoiceBell **permanently deletes all local data**.

### Data Portability

Since all data is local, you can:

- Export via Android backup/restore
- Manually backup app data (if rooted)
- Migrate to new device via Android device-to-device transfer

## Changes to Privacy Policy

We may update this Privacy Policy to reflect:

- Changes in Android permissions requirements
- New features added to VoiceBell
- Legal or regulatory changes

**How we notify you:**
- Updated "Last Updated" date at top
- Announcement in app release notes
- GitHub repository update

**Material changes** will be announced in the app and will require re-acceptance.

## Distribution Channels

VoiceBell is distributed through:

1. **F-Droid** - Privacy-respecting app store
2. **GitHub Releases** - Direct APK downloads
3. **Google Play Store** - Mainstream distribution
4. **Obtainium** - Self-updateable APKs

**Privacy note**: These stores may collect their own analytics about app downloads. We have no control over or access to this data.

## Open Source Transparency

VoiceBell is fully open source under GPL-3.0:

- **Source code**: Available on GitHub
- **No hidden code**: Everything is auditable
- **Community review**: Anyone can verify privacy claims
- **No proprietary SDKs**: Only open-source dependencies

**Verify our privacy claims**: Read the source code at [github.com/yourusername/voicebell](https://github.com/yourusername/voicebell)

## Legal Basis (GDPR)

Although VoiceBell doesn't collect personal data, here's our GDPR compliance:

- **Lawful basis**: Not applicable (no data collection)
- **Data minimization**: Ultimate compliance (zero data)
- **Purpose limitation**: Not applicable
- **Storage limitation**: Data stored locally only
- **Right to erasure**: Uninstall deletes all data
- **Data portability**: Android backup system

If you're in the EU: You have nothing to worry about. We can't violate GDPR if we don't collect your data.

## California Privacy Rights (CCPA)

For California residents:

- **Categories of data collected**: None
- **Data sold to third parties**: None
- **Right to know**: Not applicable (no collection)
- **Right to delete**: Uninstall the app
- **Right to opt-out**: Not applicable (nothing to opt out of)

## Contact Us

### Questions About Privacy

If you have questions about this Privacy Policy or VoiceBell's privacy practices:

- **Email**: [your-email@example.com]
- **GitHub Issues**: [github.com/yourusername/voicebell/issues](https://github.com/yourusername/voicebell/issues)
- **GitHub Discussions**: [github.com/yourusername/voicebell/discussions](https://github.com/yourusername/voicebell/discussions)

### Data Requests

Since we don't collect data, we cannot:

- Provide data exports (your data is already on your device)
- Delete data from servers (we have no servers)
- Modify privacy settings (there's nothing to modify)

For technical support, please use GitHub Issues.

## Summary

**In Plain English:**

VoiceBell is an alarm clock app that:

- ✅ Works 100% offline
- ✅ Stores everything locally on your device
- ✅ Uses offline voice recognition (Vosk)
- ✅ Has NO internet permission
- ✅ Collects ZERO data
- ✅ Sends NOTHING to servers
- ✅ Is fully open source

**Your alarm data stays on your phone. Period.**

---

**VoiceBell Privacy Policy v1.0**

*This privacy policy is provided in the spirit of transparency and user empowerment. We believe privacy is a fundamental right, not a privilege.*
