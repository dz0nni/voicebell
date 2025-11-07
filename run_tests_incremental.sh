#!/bin/bash

# Incremental testimine - ei puhasta cache, kasutab olemasolevat
# See peaks olema PALJU kiirem

set -e

echo "=== VoiceBell Clock - Incremental Unit Testing ==="
echo ""

# Seadista ANDROID_HOME
export ANDROID_HOME=/opt/android-sdk
echo "✓ ANDROID_HOME seadistatud: $ANDROID_HOME"

# Seadista Gradle JVM heap size piirangud
export GRADLE_OPTS="-Xmx1536m -Xms256m -XX:MaxMetaspaceSize=384m -XX:+UseG1GC"
echo "✓ Gradle mälu piiratud: Max 1.5GB heap, 384MB metaspace"

# Kontrolli SDK olemasolu
if [ ! -d "$ANDROID_HOME" ]; then
    echo "✗ VIGA: Android SDK ei leitud asukohas $ANDROID_HOME"
    exit 1
fi

echo "✓ Android SDK leitud"
echo "✓ Kasutan olemasolevat build cache (EI puhasta)"
echo ""

# Käivita ainult debug unit testid - kasuta olemasolevat cache't
echo "▶ Käivitan AINULT Debug Unit teste (incremental)..."
echo "  (Kasutan --max-workers=1)"
echo "  (Timeout: 5 minutit)"
echo ""

# Kasuta timeout'd et vältida hangumist - lühem timeout sest cache on olemas
timeout 300 ./gradlew testDebugUnitTest \
    --no-daemon \
    --max-workers=1 \
    --no-parallel \
    2>&1 | tee test_incremental_run.log

EXIT_CODE=${PIPESTATUS[0]}

echo ""
if [ $EXIT_CODE -eq 124 ]; then
    echo "⚠ HOIATUS: Testid aegusid välja (timeout 5 minutit)"
elif [ $EXIT_CODE -eq 0 ]; then
    echo "✓ Testid lõpetatud edukalt"
    echo ""
    echo "Testide tulemused:"
    grep -E "(BUILD|tests|passed|failed)" test_incremental_run.log | tail -20
else
    echo "✗ Testid ebaõnnestusid (exit code: $EXIT_CODE)"
fi

echo ""
echo "=== Testimine lõpetatud ==="
echo "✓ Logi salvestatud: test_incremental_run.log"

exit $EXIT_CODE
