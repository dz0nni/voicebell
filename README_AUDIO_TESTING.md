# VoiceBell Voice Command Audio Testing

This guide explains how to generate and use test audio files for testing VoiceBell voice commands.

## 🎤 Test Commands

We have 5 different test commands:

1. **test_set_timer_2min.wav** - "set timer for 2 minutes"
2. **test_set_timer_30sec.wav** - "set timer for 30 seconds"
3. **test_set_alarm_7am.wav** - "set alarm for 7 AM"
4. **test_wake_up_630.wav** - "wake me up at 6 30"
5. **test_set_timer_5min.wav** - "set timer for 5 minutes"

## 🔧 Setup

### Option 1: Auto-generate with Python (Recommended)

```bash
# Install dependencies
pip3 install gtts pydub
brew install ffmpeg  # If not already installed

# Generate audio files
cd /Users/kratt/claude/clock-testing
python3 generate_test_audio.py
```

This will create a `test_audio/` directory with all 5 WAV files.

### Option 2: Manual Recording

Record yourself saying each command and convert to 16kHz mono WAV:

```bash
ffmpeg -i your_recording.m4a -ar 16000 -ac 1 test_set_timer_2min.wav
```

## 📱 Testing on Android Emulator

### Method 1: Direct File Playback (Doesn't work for mic input)

```bash
# Transfer files to emulator
adb push test_audio/*.wav /sdcard/Download/

# Play file (this plays through speakers, NOT mic)
adb shell am start -a android.intent.action.VIEW \
  -d file:///sdcard/Download/test_set_timer_2min.wav
```

❌ **Problem**: This plays through emulator speakers, not microphone input.

### Method 2: Virtual Audio Cable (macOS/Windows)

This is the **correct way** to test microphone input on emulator:

#### macOS Setup:

1. **Install BlackHole** (virtual audio device):
   ```bash
   brew install blackhole-2ch
   ```

2. **Create Multi-Output Device** in Audio MIDI Setup:
   - Open "Audio MIDI Setup" app
   - Click "+" → "Create Multi-Output Device"
   - Check both "BlackHole 2ch" and your speakers
   - Name it "Emulator Output"

3. **Create Aggregate Device**:
   - Click "+" → "Create Aggregate Device"
   - Check "BlackHole 2ch" as input
   - Name it "Emulator Input"

4. **Configure Emulator**:
   ```bash
   # Restart emulator with audio input
   ~/Library/Android/sdk/emulator/emulator -avd Pixel_7_Pro_API_36 \
     -audio-in-backend coreaudio
   ```

5. **Set System Audio**:
   - System Preferences → Sound → Output → "Emulator Output"
   - System Preferences → Sound → Input → "BlackHole 2ch"

6. **Play Audio**:
   ```bash
   afplay test_audio/test_set_timer_2min.wav
   ```

Now the audio plays through BlackHole, which the emulator captures as microphone input!

#### Windows Setup:

1. **Install VB-Audio Virtual Cable**: https://vb-audio.com/Cable/
2. **Set VB-Audio as default playback device**
3. **Configure emulator to use VB-Audio as mic input**
4. **Play WAV files** - they'll be captured by emulator mic

### Method 3: adb shell input (Text-based simulation)

⚠️ This doesn't test Vosk, but tests the command execution logic:

```bash
# Simulate voice command result
adb shell am broadcast \
  -a com.voicebell.clock.VOICE_COMMAND_RESULT \
  --es text "set timer for 2 minutes" \
  --ez success true
```

## 🧪 Testing Workflow

### 1. Start logcat monitoring:
```bash
adb logcat -v time | grep -E "Vosk|VoiceCommand|ExecuteVoice"
```

### 2. Open VoiceBell and navigate to Voice Command

### 3. Press and hold microphone button

### 4. Play test audio file:
```bash
# macOS with BlackHole setup:
afplay test_audio/test_set_timer_2min.wav

# OR use VLC/ffplay:
ffplay -nodisp -autoexit test_audio/test_set_timer_2min.wav
```

### 5. Release microphone button

### 6. Check logs for:
```
VoskWrapper: Partial result: { "partial" : "set timer" }
VoskWrapper: Final result: { "text" : "set timer for 2 minutes" }
VoiceRecognitionService: Recognized text: set timer for 2 minutes
ExecuteVoiceCommandUseCase: Executing command: set timer for 2 minutes
```

### 7. Verify timer was created in app

## 📝 Quick Test Commands

```bash
# Test 1: 2-minute timer
afplay test_audio/test_set_timer_2min.wav

# Test 2: 30-second timer
afplay test_audio/test_set_timer_30sec.wav

# Test 3: 7 AM alarm
afplay test_audio/test_set_alarm_7am.wav

# Test 4: 6:30 alarm
afplay test_audio/test_wake_up_630.wav

# Test 5: 5-minute timer
afplay test_audio/test_set_timer_5min.wav
```

## 🐛 Troubleshooting

### No audio captured in emulator:
- ✓ Verify BlackHole is installed and selected
- ✓ Check emulator audio settings
- ✓ Restart emulator with `-audio-in-backend coreaudio`

### Vosk returns empty text:
- ✓ Check audio format (must be 16kHz, mono, WAV)
- ✓ Verify Vosk model is downloaded in app
- ✓ Check if "Enable Voice Commands" is ON in settings

### Command not executed:
- ✓ Check logs for parsing errors
- ✓ Verify command syntax matches expected patterns
- ✓ Check ExecuteVoiceCommandUseCase logs

## 📊 Expected Results

| Test File | Expected Result |
|-----------|----------------|
| test_set_timer_2min.wav | Creates 2-minute timer |
| test_set_timer_30sec.wav | Creates 30-second timer |
| test_set_alarm_7am.wav | Creates alarm at 07:00 |
| test_wake_up_630.wav | Creates alarm at 06:30 |
| test_set_timer_5min.wav | Creates 5-minute timer |

## 🔗 Alternative: Online TTS Services

If you can't use Python, generate files online:

1. **Google Cloud TTS**: https://cloud.google.com/text-to-speech
2. **Amazon Polly**: https://aws.amazon.com/polly/
3. **Natural Readers**: https://www.naturalreaders.com/online/

Then convert to 16kHz mono WAV:
```bash
ffmpeg -i downloaded.mp3 -ar 16000 -ac 1 output.wav
```

## ✅ Success Criteria

A successful test should show:
1. ✅ Microphone button turns red ("Listening...")
2. ✅ Vosk partial results appear in logs
3. ✅ Final result contains correct text
4. ✅ Command is executed (timer/alarm created)
5. ✅ UI shows confirmation message
