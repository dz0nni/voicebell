#!/bin/bash

# Turvaline testide käivitamise skript v2
# Seadistab ANDROID_HOME ja piirab ressursikasutust agressiivselt

set -e

echo "=== VoiceBell Clock - Turvaline Unit Testing ==="
echo ""

# Seadista ANDROID_HOME
export ANDROID_HOME=/opt/android-sdk
echo "✓ ANDROID_HOME seadistatud: $ANDROID_HOME"

# Seadista Gradle JVM heap size piirangud - VÄGA agressiivsed
export GRADLE_OPTS="-Xmx1536m -Xms256m -XX:MaxMetaspaceSize=384m -XX:+UseG1GC"
echo "✓ Gradle mälu piiratud: Max 1.5GB heap, 384MB metaspace"

# Kontrolli SDK olemasolu
if [ ! -d "$ANDROID_HOME" ]; then
    echo "✗ VIGA: Android SDK ei leitud asukohas $ANDROID_HOME"
    exit 1
fi

echo "✓ Android SDK leitud"
echo ""

# Puhasta vanad build failid et vähendada mälu vajadust
echo "▶ Puhastamine..."
./gradlew clean --no-daemon 2>&1 | grep -E "(BUILD|FAILED|Task)" || true
echo ""

# Käivita ainult debug unit testid - väga piiratult
echo "▶ Käivitan AINULT Debug Unit teste..."
echo "  (Kasutan --max-workers=1 ja --no-parallel)"
echo "  (Timeout: 8 minutit)"
echo ""

# Kasuta timeout'd et vältida hangumist
timeout 480 ./gradlew testDebugUnitTest \
    --no-daemon \
    --max-workers=1 \
    --no-parallel \
    --info \
    2>&1 | tee test_safe_run.log

EXIT_CODE=${PIPESTATUS[0]}

echo ""
if [ $EXIT_CODE -eq 124 ]; then
    echo "⚠ HOIATUS: Testid aegusid välja (timeout 8 minutit)"
elif [ $EXIT_CODE -eq 0 ]; then
    echo "✓ Testid lõpetatud edukalt"
else
    echo "✗ Testid ebaõnnestusid (exit code: $EXIT_CODE)"
fi

echo ""
echo "=== Testimine lõpetatud ==="
echo "✓ Logi salvestatud: test_safe_run.log"
echo ""
echo "Vaata teste:"
echo "  grep -E '(tests|passed|failed|BUILD)' test_safe_run.log"

exit $EXIT_CODE
