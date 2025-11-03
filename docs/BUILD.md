# Build Instructions

This document provides detailed instructions for building VoiceBell from source.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Build Variants](#build-variants)
- [Building for Release](#building-for-release)
- [Voice Model Setup](#voice-model-setup)
- [Troubleshooting](#troubleshooting)

## Prerequisites

### Required Software

1. **Java Development Kit (JDK) 17**
   ```bash
   # Check version
   java -version

   # Should show: openjdk version "17.x.x"
   ```

   **Download**: [Oracle JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) or [AdoptOpenJDK](https://adoptopenjdk.net/)

2. **Android Studio**
   - Version: Arctic Fox (2020.3.1) or newer recommended
   - Download: [developer.android.com/studio](https://developer.android.com/studio)

3. **Android SDK**
   - Minimum SDK: API 29 (Android 10)
   - Target SDK: API 35 (Android 15)
   - Build Tools: 34.0.0 or higher

   Install via Android Studio SDK Manager:
   - Tools → SDK Manager → SDK Platforms → Android 10.0+
   - Tools → SDK Manager → SDK Tools → Android SDK Build-Tools

4. **Git**
   ```bash
   git --version
   ```

### Optional Tools

- **ADB (Android Debug Bridge)**: For installing APKs
- **Gradle Wrapper**: Included in project (no separate installation needed)

## Quick Start

### 1. Clone Repository

```bash
git clone https://github.com/yourusername/voicebell.git
cd voicebell
```

### 2. Download Voice Recognition Model

VoiceBell requires the Vosk offline speech recognition model:

```bash
# Create models directory
mkdir -p app/src/main/assets/models

# Download model (Linux/Mac)
curl -L https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip -o model.zip
unzip model.zip -d app/src/main/assets/models/
rm model.zip

# Windows (PowerShell)
# Invoke-WebRequest -Uri "https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip" -OutFile "model.zip"
# Expand-Archive -Path model.zip -DestinationPath app/src/main/assets/models/
# Remove-Item model.zip
```

**Note**: The small model (~40MB) is recommended for mobile. Larger models provide better accuracy but increase APK size.

### 3. Open in Android Studio

1. Launch Android Studio
2. File → Open
3. Select the `voicebell` directory
4. Wait for Gradle sync to complete (first sync may take 5-10 minutes)

### 4. Build Debug APK

**Command Line:**
```bash
./gradlew assembleDebug
```

**Android Studio:**
- Build → Make Project (Ctrl+F9)
- Build → Build Bundle(s) / APK(s) → Build APK(s)

Output: `app/build/outputs/apk/debug/app-debug.apk`

### 5. Install on Device

**Via ADB:**
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

**Via Android Studio:**
- Run → Run 'app' (Shift+F10)
- Select connected device or emulator

## Build Variants

VoiceBell has two build types:

### Debug Build

Development build with debug symbols and logging:

```bash
./gradlew assembleDebug
```

**Characteristics:**
- Package name: `com.voicebell.clock.debug`
- Version suffix: `-debug`
- Debuggable: Yes
- Minification: Disabled
- ProGuard: Disabled
- APK size: Larger (~60MB)

### Release Build

Production build optimized for distribution:

```bash
./gradlew assembleRelease
```

**Characteristics:**
- Package name: `com.voicebell.clock`
- Debuggable: No
- Minification: Enabled (R8)
- ProGuard: Enabled
- APK size: Smaller (~45MB)
- Requires signing for installation

## Building for Release

### 1. Generate Signing Key

Create a keystore for signing your APK:

```bash
keytool -genkey -v -keystore voicebell-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias voicebell
```

**Prompts:**
- Enter keystore password (remember this!)
- Re-enter password
- Enter your name, organization, etc.
- Enter key password (can be same as keystore password)

**Store securely:**
- Keep `voicebell-release-key.jks` safe (NEVER commit to Git)
- Backup to secure location
- Loss of key = cannot update app on Play Store

### 2. Configure Signing

Create `keystore.properties` in project root:

```properties
storePassword=YOUR_KEYSTORE_PASSWORD
keyPassword=YOUR_KEY_PASSWORD
keyAlias=voicebell
storeFile=../voicebell-release-key.jks
```

**IMPORTANT**: Add to `.gitignore`:
```
keystore.properties
*.jks
*.keystore
```

### 3. Update build.gradle

Add to `app/build.gradle.kts`:

```kotlin
// Load keystore properties
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    // ...

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ... rest of release config
        }
    }
}
```

### 4. Build Signed APK

```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

**Verify signature:**
```bash
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk
```

### 5. Build App Bundle (for Google Play)

```bash
./gradlew bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

## Voice Model Setup

### Choosing a Model

Vosk offers several English models:

| Model | Size | Accuracy | Speed | Use Case |
|-------|------|----------|-------|----------|
| `vosk-model-small-en-us-0.15` | 40MB | Good | Fast | **Recommended** for mobile |
| `vosk-model-en-us-0.22` | 1.8GB | Excellent | Slower | Desktop/high-end devices |
| `vosk-model-en-us-0.42-lgraph` | 128MB | Very Good | Medium | Alternative for better accuracy |

**For VoiceBell**: Use `vosk-model-small-en-us-0.15`

### Manual Model Installation

1. Download from [alphacephei.com/vosk/models](https://alphacephei.com/vosk/models)
2. Extract to `app/src/main/assets/models/`
3. Ensure folder structure:
   ```
   app/src/main/assets/models/vosk-model-small-en-us-0.15/
   ├── am/
   ├── conf/
   ├── graph/
   └── ivector/
   ```

### Alternative: Download on First Launch

To reduce APK size, download model on first app launch:

1. Remove model from `assets/`
2. Implement download logic in app
3. Store in `context.filesDir/models/`

**Trade-off**: Initial download requires internet (conflicts with offline-first design).

## Gradle Tasks

### Common Tasks

```bash
# Clean build directory
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Build AAB for Play Store
./gradlew bundleRelease

# Run unit tests
./gradlew test

# Run instrumentation tests
./gradlew connectedAndroidTest

# Run all checks (lint + tests)
./gradlew check

# Generate lint report
./gradlew lint

# List all tasks
./gradlew tasks
```

### Advanced Tasks

```bash
# Install debug APK on connected device
./gradlew installDebug

# Uninstall app from device
./gradlew uninstallDebug

# Run tests and generate coverage report
./gradlew testDebugUnitTestCoverage

# Generate dependency tree
./gradlew app:dependencies

# Check for dependency updates
./gradlew dependencyUpdates  # (requires plugin)
```

## Build Configuration

### Gradle Properties

Edit `gradle.properties`:

```properties
# Increase Gradle memory
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8

# Enable parallel builds
org.gradle.parallel=true

# Enable Gradle daemon
org.gradle.daemon=true

# Enable configuration cache
org.gradle.configuration-cache=true
```

### ProGuard Rules

ProGuard configuration in `app/proguard-rules.pro`:

```proguard
# Keep Vosk classes
-keep class org.vosk.** { *; }

# Keep Room entities
-keep @androidx.room.Entity class *

# Keep Hilt generated classes
-keep class dagger.** { *; }
```

## Troubleshooting

### Build Fails: "SDK location not found"

**Problem**: Android SDK path not configured

**Solution 1**: Create `local.properties`:
```properties
sdk.dir=/path/to/Android/Sdk
```

**Solution 2**: Set environment variable:
```bash
export ANDROID_HOME=/path/to/Android/Sdk
```

### Build Fails: "Could not find model"

**Problem**: Vosk model not downloaded

**Solution**: Follow [Voice Model Setup](#voice-model-setup)

### Build Fails: "Execution failed for task ':app:lintVitalRelease'"

**Problem**: Lint errors blocking release build

**Solution**: Fix lint errors or add to `build.gradle.kts`:
```kotlin
android {
    lintOptions {
        isAbortOnError = false
    }
}
```

### Build Fails: "Out of memory"

**Problem**: Insufficient Gradle memory

**Solution**: Increase memory in `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m
```

### APK Size Too Large

**Problem**: APK exceeds 100MB

**Solutions:**
1. Remove larger Vosk model
2. Use App Bundle instead of APK
3. Enable APK splits:
   ```kotlin
   android {
       splits {
           abi {
               isEnable = true
               reset()
               include("armeabi-v7a", "arm64-v8a", "x86_64")
           }
       }
   }
   ```

### App Crashes on Vosk Initialization

**Problem**: Vosk model path incorrect

**Solution**: Verify path in code:
```kotlin
val modelPath = "${context.filesDir}/models/vosk-model-small-en-us-0.15"
// Check if directory exists
require(File(modelPath).exists()) { "Model not found" }
```

### R8 Minification Issues

**Problem**: Release build crashes but debug works

**Solution**: Add ProGuard keep rules for affected classes

### Slow Build Times

**Solutions:**
1. Enable Gradle daemon: `org.gradle.daemon=true`
2. Enable parallel builds: `org.gradle.parallel=true`
3. Increase memory: `org.gradle.jvmargs=-Xmx4096m`
4. Disable unnecessary tasks during development
5. Use `--offline` flag when internet not needed

## CI/CD (Future)

### GitHub Actions (Planned)

Automated builds on every commit:

```yaml
# .github/workflows/build.yml
name: Build APK
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Build with Gradle
        run: ./gradlew assembleDebug
```

## F-Droid Build

F-Droid builds from source using their own infrastructure.

See [F-DROID.md](F-DROID.md) for submission details.

**Requirements:**
- Reproducible builds
- No proprietary dependencies
- All build scripts in Git

## Performance Optimization

### Build Performance

1. **Use latest Gradle**: Update `gradle-wrapper.properties`
2. **Enable configuration cache**: Speeds up incremental builds
3. **Use build cache**: Shares build outputs between projects
4. **Exclude unnecessary resources**: Reduce APK size

### Runtime Performance

1. **Use R8**: Enabled by default in release builds
2. **Optimize images**: Use WebP format
3. **Enable ProGuard**: Remove unused code

## Further Reading

- [Android Developer Guide](https://developer.android.com/studio/build)
- [Gradle User Manual](https://docs.gradle.org/current/userguide/userguide.html)
- [Vosk Documentation](https://alphacephei.com/vosk/)
- [ProGuard Manual](https://www.guardsquare.com/manual/home)

---

**Questions?** Open an issue on [GitHub](https://github.com/yourusername/voicebell/issues)
