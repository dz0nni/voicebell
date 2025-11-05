# Testing Vosk Integration - Detailed Instructions

**Date:** 2025-11-05
**Branch:** `feature/alarm-improvements`
**Commit:** `c3c6549292039e6698f317102ce057e42485f498`
**Feature:** Offline Voice Recognition with Vosk

---

## 📋 Pre-Testing Checklist

Before starting, ensure you have:

- [ ] Android device or emulator (Android 10+ / API 29+)
- [ ] Android SDK installed and configured
- [ ] Internet connection (for first-time model download)
- [ ] ~100 MB free storage on device (40 MB model + APK)
- [ ] Microphone access available
- [ ] Working directory: `/docker/tannu/claude/clock-features`

---

## 🔧 Phase 1: Build & Installation

### Step 1.1: Verify Git Status

```bash
cd /docker/tannu/claude/clock-features
git status
git log -1 --oneline
```

**Expected output:**
```
On branch feature/alarm-improvements
nothing to commit, working tree clean
c3c6549 Implement Vosk offline voice recognition integration
```

### Step 1.2: Clean Build

```bash
./gradlew clean
```

### Step 1.3: Build Debug APK

```bash
./gradlew assembleDebug
```

**Expected:**
- Build should succeed without errors
- APK location: `app/build/outputs/apk/debug/app-debug.apk`
- APK size: ~15-25 MB (without Vosk model)

**Common Issues:**

❌ **Issue:** `SDK location not found`
✅ **Fix:** Set `ANDROID_HOME` or create `local.properties`:
```bash
echo "sdk.dir=/path/to/android/sdk" > local.properties
```

❌ **Issue:** `Vosk library not found`
✅ **Fix:** Check `app/build.gradle.kts` lines 144-145:
```kotlin
implementation("net.java.dev.jna:jna:5.13.0@aar")
implementation("com.alphacephei:vosk-android:0.3.45")
```

❌ **Issue:** Compilation errors in Vosk files
✅ **Fix:** Ensure all 6 new files were committed:
```bash
git ls-files | grep -E "(vosk|voice)"
```

### Step 1.4: Install APK

**Option A: Physical Device via ADB**
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

**Option B: Emulator**
```bash
adb -e install app/build/outputs/apk/debug/app-debug.apk
```

**Option C: Manual Install**
- Copy APK to device
- Enable "Install from Unknown Sources"
- Tap APK file to install

---

## 🧪 Phase 2: Vosk Model Download Testing

### Step 2.1: Launch App

1. Open **VoiceBell** app
2. Grant required permissions if prompted:
   - ✅ Alarms & Reminders
   - ✅ Notifications
   - ❌ Do NOT grant microphone yet (test later)

### Step 2.2: Navigate to Settings

1. Tap **Settings** icon (⚙️) in top right
2. Scroll to **"Voice Commands"** section
3. Look for **"Voice Recognition Model"** card

**Expected UI:**
```
┌─────────────────────────────────────────┐
│ Voice Recognition Model                 │
│ Download required (~40 MB)              │
│                              [Download] │
│                                         │
│ Voice commands require offline speech  │
│ recognition model. Download over WiFi  │
│ recommended.                            │
└─────────────────────────────────────────┘
```

### Step 2.3: Test Model Download

1. **Ensure WiFi connection** (40 MB download)
2. Tap **"Download"** button

**Expected behavior:**

- Button changes to circular progress indicator
- Linear progress bar appears below
- Progress updates: 0% → 100%
- Download phases:
  - 0-70%: Downloading ZIP from alphacephei.com
  - 70-100%: Extracting ZIP

**Timeline:**
- Good connection: 30-60 seconds
- Slow connection: 2-5 minutes

**During download, check logcat:**
```bash
adb logcat | grep -i vosk
```

**Expected logs:**
```
VoskModelManager: Starting model download from https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip
VoskModelManager: File size: 40 MB
VoskModelManager: Extracting model...
VoskModelManager: Model downloaded and extracted successfully
```

### Step 2.4: Verify Download Success

**UI should change to:**
```
┌─────────────────────────────────────────┐
│ Voice Recognition Model                 │
│ Model installed (~40 MB)                │
│                                    [🗑️] │
└─────────────────────────────────────────┘
```

**Verify file system:**
```bash
adb shell ls -lh /data/data/com.voicebell.clock/files/vosk-model/
```

**Expected output:**
- Multiple model files (.mdl, .conf, etc.)
- Total size: ~40 MB

### Step 2.5: Test Model Deletion (Optional)

1. Tap **delete icon** (🗑️)
2. Model should be removed
3. Card should return to "Download required" state
4. **Re-download** before proceeding to Phase 3

---

## 🎤 Phase 3: Voice Recognition Testing

### Step 3.1: Enable Voice Commands

In Settings → Voice Commands:
1. Toggle **"Enable Voice Commands"** to ON
2. Keep Settings open for now

### Step 3.2: Grant Microphone Permission

**Method A: Via Voice Command Screen**
1. Navigate back to Home
2. Tap **microphone button** (large button in Experimental view, or voice icon in Classic view)
3. VoiceCommandScreen opens
4. Tap microphone button
5. Android requests **RECORD_AUDIO** permission
6. Grant permission

**Method B: Via System Settings**
1. Go to System Settings → Apps → VoiceBell → Permissions
2. Grant **Microphone** permission

### Step 3.3: Test Push-to-Talk

1. Open **VoiceCommandScreen** (microphone button)
2. Observe initial state:
   - Text: "Press and hold to speak"
   - Large circular microphone button (blue/primary color)
   - Examples at bottom

3. **Press and HOLD** the microphone button
4. Observe changes:
   - Button turns RED
   - Text: "Listening... Speak now"
   - Pulsing animation around button
   - Notification appears: "Voice Recognition - Listening..."

5. **While holding**, speak clearly: **"Set alarm for 7 AM"**

6. **Release** the button

**Expected behavior:**
- Button returns to blue
- "Processing..." brief state
- Result card appears (green background):
  ```
  ┌─────────────────────────────────────┐
  │ Alarm set for 7:00 AM               │
  └─────────────────────────────────────┘
  ```

7. Navigate to Alarms tab
8. **Verify:** New alarm at 7:00 AM exists, enabled

### Step 3.4: Test Multiple Voice Commands

Test each command **3 times** for consistency:

**Alarm Commands:**
| Command | Expected Result | Pass/Fail |
|---------|----------------|-----------|
| "Set alarm for 7 AM" | Alarm created at 07:00 | ⬜ |
| "Wake me up at 6:30" | Alarm created at 06:30 | ⬜ |
| "Set alarm for 8 o'clock" | Alarm created at 08:00 | ⬜ |
| "Alarm at 9:15 PM" | Alarm created at 21:15 | ⬜ |
| "Wake me up at seven thirty" | Alarm created at 07:30 | ⬜ |

**Timer Commands:**
| Command | Expected Result | Pass/Fail |
|---------|----------------|-----------|
| "Set timer for 5 minutes" | Timer starts (5:00) | ⬜ |
| "Timer for 10 seconds" | Timer starts (0:10) | ⬜ |
| "Set timer for 1 hour" | Timer starts (1:00:00) | ⬜ |
| "Timer 30 minutes" | Timer starts (30:00) | ⬜ |
| "Set timer for 2 hours and 15 minutes" | Timer starts (2:15:00) | ⬜ |

**Invalid/Edge Cases:**
| Command | Expected Result | Pass/Fail |
|---------|----------------|-----------|
| "Hello" | Error: "I didn't understand" | ⬜ |
| (silence) | Error: "No speech detected" | ⬜ |
| "Set alarm" (no time) | Error: "Could not understand time" | ⬜ |
| "Timer" (no duration) | Error: "Could not understand duration" | ⬜ |

### Step 3.5: Test Microphone Access Scenarios

**Scenario A: Permission Denied**
1. Revoke microphone permission (System Settings)
2. Try voice command
3. **Expected:** Error message or permission prompt

**Scenario B: Model Not Downloaded**
1. Delete Vosk model (Settings)
2. Try voice command
3. **Expected:** Error: "Voice model not downloaded"

**Scenario C: Background Noise**
1. Play music/noise in background
2. Try voice command
3. **Expected:** May fail to recognize OR recognize incorrectly
4. Note: Vosk is designed for quiet environments

---

## 🔍 Phase 4: Service & System Integration

### Step 4.1: Verify Foreground Service

**While using voice command:**
```bash
adb shell dumpsys activity services | grep VoiceRecognition
```

**Expected output:**
```
ServiceRecord{...com.voicebell.clock/.service.VoiceRecognitionService}
isForeground=true
```

**Check notification:**
- Pull down notification shade while listening
- Should see: "Voice Recognition - Listening..."

### Step 4.2: Test Service Lifecycle

1. Start voice listening (hold button)
2. Press Home button (minimize app)
3. **Expected:** Service continues, notification persists
4. Return to app
5. Release button
6. **Expected:** Service stops, notification disappears

### Step 4.3: Test Memory & Performance

**Before voice command:**
```bash
adb shell dumpsys meminfo com.voicebell.clock | grep TOTAL
```

**During voice listening:**
```bash
adb shell dumpsys meminfo com.voicebell.clock | grep TOTAL
```

**Expected:**
- Memory increase: ~30-50 MB (Vosk model loaded)
- No memory leaks after stopping
- App should not crash with low memory

### Step 4.4: Test Battery Impact

**Enable battery profiling:**
```bash
adb shell dumpsys batterystats --enable full-wake-history
adb shell dumpsys batterystats --reset
```

**Use voice commands for 5 minutes**

**Check battery usage:**
```bash
adb shell dumpsys batterystats | grep voicebell
```

**Expected:**
- Push-to-talk: Minimal battery (only when button held)
- Should NOT drain battery when idle

---

## 🐛 Phase 5: Error Handling & Edge Cases

### Test 5.1: Rapid Button Presses

1. Quickly tap microphone button 10 times
2. **Expected:** No crashes, service handles gracefully

### Test 5.2: Network Interruption During Download

1. Start model download
2. Turn off WiFi at 50%
3. **Expected:** Download fails, error shown, cleanup occurs

### Test 5.3: App Killed During Listening

1. Start voice listening
2. Kill app: `adb shell am force-stop com.voicebell.clock`
3. Reopen app
4. **Expected:** No corrupted state, service cleaned up

### Test 5.4: Device Rotation

1. Start voice listening
2. Rotate device
3. **Expected:** UI recreates, service continues

### Test 5.5: Low Storage

1. Fill device storage to <100 MB
2. Try to download model
3. **Expected:** Error: "Insufficient storage"

---

## 📊 Phase 6: Logcat Analysis

### Collect Full Logs

```bash
adb logcat -c  # Clear logs
# Perform voice command
adb logcat -d > vosk_test_logs.txt
```

### Key Log Tags to Check

```bash
# Vosk wrapper
adb logcat -s VoskWrapper:* | grep -E "(init|accept|final|error)"

# Voice recognition service
adb logcat -s VoiceRecognitionService:* | grep -E "(start|stop|error|result)"

# Model manager
adb logcat -s VoskModelManager:* | grep -E "(download|extract|delete)"

# Command execution
adb logcat -s ExecuteVoiceCommand:* | grep -E "(executing|success|error)"
```

### Expected Log Flow (Happy Path)

```
VoskModelManager: Model exists: true
VoiceRecognitionService: Service created
VoiceRecognitionService: Start listening action received
VoskWrapper: Loading Vosk model from: /data/data/.../vosk-model
VoskWrapper: Vosk model initialized successfully
VoiceRecognitionService: Recording started
VoskWrapper: Partial result: {"partial":"set"}
VoskWrapper: Partial result: {"partial":"set alarm"}
VoskWrapper: Final result: {"text":"set alarm for seven am"}
VoiceRecognitionService: Recognized text: set alarm for seven am
ExecuteVoiceCommand: Executing voice command: set alarm for seven am
ExecuteVoiceCommand: Creating alarm for 07:00
VoiceRecognitionService: Recording stopped
```

---

## ✅ Phase 7: Success Criteria

### Mandatory Checks (All must pass)

- [ ] APK builds without errors
- [ ] App installs successfully
- [ ] Model download completes (40 MB)
- [ ] Model extracts without errors
- [ ] Microphone permission granted
- [ ] Voice recognition service starts
- [ ] At least 5/5 alarm commands work
- [ ] At least 5/5 timer commands work
- [ ] Alarms actually created in DB
- [ ] Timers actually start countdown
- [ ] UI shows success confirmation
- [ ] No crashes during normal use
- [ ] Service stops after button release
- [ ] No memory leaks
- [ ] Works 100% offline (airplane mode)

### Optional Checks (Nice to have)

- [ ] Model deletion works
- [ ] Re-download works after deletion
- [ ] Handles low storage gracefully
- [ ] Handles permission denial gracefully
- [ ] Error messages are user-friendly
- [ ] Animations smooth (60 FPS)
- [ ] Notification visible during listening
- [ ] Background noise handling

---

## 🚨 Known Limitations & Expected Behavior

### Current Limitations:

1. **English Only:** Vosk model is English (US). Other languages will fail.
2. **Quiet Environment:** Works best without background noise.
3. **Clear Speech:** Mumbling or fast speech may not work.
4. **Internet for Download:** First-time download needs internet.
5. **No Wake Word:** Must press button (no "Hey VoiceBell" activation).
6. **No TTS Feedback:** No voice confirmation (TTS planned for next phase).

### Known Issues to Ignore:

1. **Model Download Slow:** 40 MB takes time on slow connections (expected).
2. **First Recognition Slow:** Model loading ~2-3 seconds (expected).
3. **Partial Results:** May see incomplete text briefly (expected, for debugging).
4. **No Cancel Command:** "Cancel alarm" not implemented yet (planned).

---

## 📝 Bug Report Template

If you find bugs, report using this format:

```markdown
**Bug:** [Short description]

**Steps to Reproduce:**
1.
2.
3.

**Expected Behavior:**


**Actual Behavior:**


**Logcat Output:**
```
[Paste relevant logs]
```

**Device Info:**
- Device: [e.g., Pixel 6]
- Android Version: [e.g., Android 13]
- VoiceBell Version: 0.1.4 (feature/alarm-improvements)
- Commit: c3c6549

**Screenshot/Video:**
[If applicable]
```

---

## 🎯 Testing Priority

### Priority 1 (CRITICAL):
1. Model download
2. Voice recognition starts
3. Alarm creation from voice
4. Timer creation from voice

### Priority 2 (HIGH):
5. Error handling
6. Service lifecycle
7. Permission handling

### Priority 3 (MEDIUM):
8. Performance & battery
9. Edge cases
10. UI/UX polish

---

## 📞 Contact

**Issues found?** Report in:
- This testing session
- Create GitHub issue: https://github.com/dz0nni/voicebell/issues
- Branch: `feature/alarm-improvements`

**Testing completed?** Report back with:
- ✅ All tests passed OR
- ⚠️ X tests failed (with bug reports)

---

**Good luck with testing! 🚀**

*Generated with Claude Code*
*Date: 2025-11-05*
