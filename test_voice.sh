#!/bin/bash
# Voice Recognition Quick Test Script
# Usage: ./test_voice.sh

set -e

PHONE="38091FDJH00H1Z"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "=========================================="
echo "  Voice Recognition Quick Test"
echo "=========================================="
echo ""

# 1. Sync
echo "🔄 Step 1/4: Syncing with dev worktree..."
cd "$SCRIPT_DIR"
git fetch --all
git checkout feature/alarm-improvements
git pull origin feature/alarm-improvements
COMMIT=$(git log -1 --oneline)
echo "✅ Synced to: $COMMIT"
echo ""

# 2. Build
echo "🏗️  Step 2/4: Building APK..."
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export ANDROID_HOME=~/Library/Android/sdk
./gradlew assembleDebug --quiet
echo "✅ APK built"
echo ""

# 3. Install
echo "📱 Step 3/4: Installing on phone $PHONE..."
adb -s $PHONE install -r app/build/outputs/apk/debug/app-debug.apk
echo "✅ Installed"
echo ""

# 4. Prepare testing
echo "📋 Step 4/4: Preparing logs..."
adb -s $PHONE logcat -c
echo "✅ Logs cleared"
echo ""

echo "=========================================="
echo "  ✅ SETUP COMPLETE!"
echo "=========================================="
echo ""
echo "📱 MANUAL TEST STEPS:"
echo ""
echo "  1. Open VoiceBell app on phone"
echo "  2. Tap microphone button"
echo "  3. Say: 'set timer 5 minutes'"
echo "  4. Tap microphone again to stop"
echo ""
echo "=========================================="
echo "  📊 Monitoring logs..."
echo "  (Press Ctrl+C to stop)"
echo "=========================================="
echo ""

# Monitor logs
adb -s $PHONE logcat | grep --line-buffered -E "(Saved partial|using last partial|Recognized text|Command executed|Timer.*minutes|Alarm.*set)"
