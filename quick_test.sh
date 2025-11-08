#!/bin/bash

# Quick test script for VoiceBell app
# This allows Claude to rapidly test the app on emulator

set -e

echo "🔍 Quick Test Script for VoiceBell"
echo "=================================="
echo ""

# Wait for device
echo "⏳ Waiting for Android device/emulator..."
adb wait-for-device
echo "✅ Device connected!"
echo ""

# Install latest APK
echo "📦 Installing VoiceBell-0.1.8-debug.apk..."
adb install -r VoiceBell-0.1.8-debug.apk
echo "✅ APK installed!"
echo ""

# Clear previous logs
adb logcat -c

# Grant microphone permission
echo "🎤 Granting RECORD_AUDIO permission..."
adb shell pm grant com.voicebell.clock.debug android.permission.RECORD_AUDIO

# Start the app
echo "🚀 Starting VoiceBell app..."
adb shell am start -n com.voicebell.clock.debug/com.voicebell.clock.MainActivity

echo ""
echo "📱 App started! Now monitoring logs..."
echo "👉 Press microphone button in the app to test"
echo "=================================="
echo ""

# Monitor logs in real-time, filtering for VoiceBell and crashes
adb logcat | grep -E "(VoiceBell|AndroidRuntime|FATAL|VoskWrapper|VoiceRecognition)"
