#!/bin/bash
set -e

# VoiceBell Release APK Signing Script
# Usage: ./sign-release.sh VoiceBell-X.Y.Z.apk

if [ -z "$1" ]; then
    echo "Error: Please provide output APK filename"
    echo "Usage: ./sign-release.sh VoiceBell-0.1.10.apk"
    exit 1
fi

OUTPUT_APK="$1"
UNSIGNED_APK="app/build/outputs/apk/release/app-release-unsigned.apk"
KEYSTORE="voicebell-release.keystore"

# Check if unsigned APK exists
if [ ! -f "$UNSIGNED_APK" ]; then
    echo "Error: Unsigned APK not found at $UNSIGNED_APK"
    echo "Please run: ./gradlew assembleRelease"
    exit 1
fi

# Check if keystore exists
if [ ! -f "$KEYSTORE" ]; then
    echo "Error: Keystore not found at $KEYSTORE"
    exit 1
fi

# Prompt for passwords securely (won't show in terminal or history)
echo "VoiceBell Release APK Signing"
echo "=============================="
read -s -p "Keystore password: " KS_PASS
echo ""
read -s -p "Key password: " KEY_PASS
echo ""

if [ -z "$KS_PASS" ] || [ -z "$KEY_PASS" ]; then
    echo "Error: Passwords cannot be empty"
    exit 1
fi

# Set Java environment
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

# Check if apksigner exists
APKSIGNER="$HOME/Library/Android/sdk/build-tools/34.0.0/apksigner"
if [ ! -f "$APKSIGNER" ]; then
    echo "Error: apksigner not found at $APKSIGNER"
    echo "Please install Android SDK build-tools 34.0.0"
    exit 1
fi

echo "Signing APK..."
$APKSIGNER sign \
  --ks "$KEYSTORE" \
  --ks-key-alias voicebell \
  --ks-pass env:KS_PASS \
  --key-pass env:KEY_PASS \
  --out "$OUTPUT_APK" \
  "$UNSIGNED_APK"

# Clear passwords from memory immediately
unset KS_PASS KEY_PASS

# Verify signature
echo ""
echo "Verifying APK signature..."
$APKSIGNER verify --print-certs "$OUTPUT_APK" | head -5

echo ""
echo "✅ APK signed successfully: $OUTPUT_APK"
echo ""
ls -lh "$OUTPUT_APK"
