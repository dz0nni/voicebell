# F-Droid Submission Guide

This document explains how to submit VoiceBell to F-Droid, the privacy-respecting Android app repository.

## Table of Contents

- [About F-Droid](#about-f-droid)
- [Prerequisites](#prerequisites)
- [Preparation Steps](#preparation-steps)
- [Creating Metadata](#creating-metadata)
- [Submission Process](#submission-process)
- [Review Process](#review-process)
- [Updating Your App](#updating-your-app)
- [Troubleshooting](#troubleshooting)

## About F-Droid

[F-Droid](https://f-droid.org/) is a catalog of Free and Open Source Software (FOSS) for Android.

**Benefits:**
- Privacy-focused user base
- No tracking or analytics
- Reproducible builds
- Automated updates
- Community trust

**Requirements:**
- 100% open source (GPL-3.0 ✓)
- No proprietary dependencies ✓
- No tracking or ads ✓
- Builds from source ✓

VoiceBell meets all F-Droid requirements.

## Prerequisites

### 1. F-Droid Account

- **GitLab account**: F-Droid uses GitLab for metadata
- Sign up: [gitlab.com](https://gitlab.com/)

### 2. Required Files

Ensure your repository has:
- [x] GPL-3.0 LICENSE file
- [x] README.md with description
- [x] CHANGELOG.md
- [x] Reproducible build configuration
- [x] No proprietary dependencies
- [x] Fastlane metadata (optional but recommended)

### 3. Git Tags

F-Droid builds from Git tags:

```bash
# Tag your release
git tag -a v1.0.0 -m "Release version 1.0.0"

# Push tags
git push --tags
```

**Important**: Tag name must match `versionName` in `build.gradle`.

## Preparation Steps

### 1. Verify Build is Reproducible

F-Droid rebuilds your app from source and verifies it matches your APK.

**Check build.gradle:**
```kotlin
android {
    buildTypes {
        release {
            // Disable dependency metadata for reproducible builds
            dependenciesInfo {
                includeInApk = false
                includeInBundle = false
            }
        }
    }
}
```

**Test local build:**
```bash
./gradlew assembleRelease

# Build again and compare
./gradlew clean assembleRelease

# Both should produce identical APK
```

### 2. Remove Non-Free Dependencies

Scan for proprietary libraries:

```bash
# Check dependencies
./gradlew app:dependencies

# Look for:
# - com.google.android.gms (Google Play Services) ❌
# - com.google.firebase ❌
# - Fabric/Crashlytics ❌
# - Proprietary analytics ❌
```

**VoiceBell status**: All dependencies are open source ✓

### 3. Prepare Fastlane Metadata

F-Droid can use Fastlane metadata for app descriptions.

**Directory structure:**
```
fastlane/
└── metadata/
    └── android/
        └── en-US/
            ├── title.txt
            ├── short_description.txt
            ├── full_description.txt
            ├── changelogs/
            │   └── 1.txt
            └── images/
                ├── icon.png
                ├── featureGraphic.png
                └── phoneScreenshots/
                    ├── 1.png
                    ├── 2.png
                    └── 3.png
```

**File requirements:**
- `title.txt`: Max 50 characters
- `short_description.txt`: Max 80 characters
- `full_description.txt`: Max 4000 characters
- `changelogs/X.txt`: Max 500 characters per version

## Creating Metadata

### 1. Fork fdroiddata Repository

```bash
# Clone F-Droid metadata repository
git clone https://gitlab.com/fdroid/fdroiddata.git
cd fdroiddata

# Create branch for your app
git checkout -b add-voicebell
```

### 2. Create Metadata File

Create `metadata/com.voicebell.clock.yml`:

```yaml
Categories:
  - Time
License: GPL-3.0-or-later
AuthorName: Your Name
AuthorEmail: your.email@example.com
WebSite: https://github.com/yourusername/voicebell
SourceCode: https://github.com/yourusername/voicebell
IssueTracker: https://github.com/yourusername/voicebell/issues
Changelog: https://github.com/yourusername/voicebell/blob/main/CHANGELOG.md

AutoName: VoiceBell
Summary: Privacy-focused offline alarm clock with voice commands

Description: |-
  VoiceBell is an alarm clock app designed with privacy as the top priority.
  All voice recognition happens 100% offline using Vosk, and the app has no
  internet permission.

  Features:
  * Alarms with repeating schedules
  * Gradual volume increase
  * Pre-alarms for deep sleepers
  * Snooze functionality
  * Timer and stopwatch
  * World clocks
  * Offline voice commands
  * Material Design 3
  * No tracking or analytics
  * Works on degoogled devices

  Voice commands work completely offline - no data is sent to any server.

RepoType: git
Repo: https://github.com/yourusername/voicebell.git

Builds:
  - versionName: 1.0.0
    versionCode: 1
    commit: v1.0.0
    subdir: app
    gradle:
      - yes
    output: build/outputs/apk/release/app-release-unsigned.apk
    prebuild: |
      # Download Vosk model if needed
      mkdir -p src/main/assets/models
      cd src/main/assets/models
      wget https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip
      unzip vosk-model-small-en-us-0.15.zip
      rm vosk-model-small-en-us-0.15.zip

AutoUpdateMode: Version v%v
UpdateCheckMode: Tags
CurrentVersion: 1.0.0
CurrentVersionCode: 1
```

### 3. Field Explanations

| Field | Description | Example |
|-------|-------------|---------|
| `Categories` | F-Droid category | `Time`, `Multimedia`, `System` |
| `License` | SPDX license identifier | `GPL-3.0-or-later` |
| `AuthorName` | Your name or organization | `VoiceBell Team` |
| `WebSite` | Project website or repo | GitHub repo URL |
| `SourceCode` | Source code URL | GitHub repo URL |
| `IssueTracker` | Bug tracker URL | GitHub Issues URL |
| `Changelog` | Changelog URL | GitHub CHANGELOG.md URL |
| `AutoName` | App name | `VoiceBell` |
| `Summary` | Short description (80 chars) | One-liner summary |
| `Description` | Full description | Detailed features list |
| `RepoType` | VCS type | `git` |
| `Repo` | Repository URL | Full Git URL |
| `Builds` | Build recipes | See below |

### 4. Build Recipe Format

```yaml
Builds:
  - versionName: 1.0.0        # Must match build.gradle
    versionCode: 1             # Must match build.gradle
    commit: v1.0.0             # Git tag or commit hash
    subdir: app                # App module directory
    gradle:                    # Use Gradle build
      - yes
    output: build/outputs/apk/release/app-release-unsigned.apk
    prebuild: |                # Commands to run before build
      # Download dependencies
      # Patch files
    scanignore:                # Files to ignore in scanner
      - app/src/main/assets/models/*.so
    antifeatures:              # If app has anti-features
      - NonFreeAssets          # For proprietary assets
```

**For VoiceBell:**
- No anti-features needed (100% free)
- `scanignore` may be needed for Vosk binaries

### 5. Anti-Features

F-Droid labels apps with "anti-features" if they:

- Track users (`Tracking`)
- Depend on non-free services (`NonFreeNet`)
- Depend on non-free software (`NonFreeDep`)
- Contain ads (`Ads`)
- Promote non-free software (`NonFreeAdd`)
- Have non-free assets (`NonFreeAssets`)

**VoiceBell**: None of these apply ✓

## Submission Process

### 1. Test Build Locally

Test that F-Droid can build your app:

```bash
# Install fdroidserver
sudo apt install fdroidserver

# Or via pip
pip3 install fdroidserver

# Test build
cd fdroiddata
fdroid build com.voicebell.clock:1

# This will:
# 1. Clone your repository
# 2. Checkout the specified commit
# 3. Run prebuild commands
# 4. Build the APK
# 5. Verify it builds successfully
```

**Expected output:**
```
Building version 1.0.0 (1) of com.voicebell.clock
...
Build successful
```

### 2. Verify Metadata

```bash
fdroid readmeta
fdroid rewritemeta com.voicebell.clock
fdroid lint com.voicebell.clock
```

Fix any warnings or errors.

### 3. Commit and Push

```bash
git add metadata/com.voicebell.clock.yml
git commit -m "New app: VoiceBell"
git push origin add-voicebell
```

### 4. Create Merge Request

1. Go to [gitlab.com/fdroid/fdroiddata](https://gitlab.com/fdroid/fdroiddata)
2. Click "Merge Requests"
3. Click "New merge request"
4. Select your branch: `add-voicebell`
5. Target branch: `master`
6. Fill in description:

```markdown
# VoiceBell - Privacy-focused offline alarm clock

## App Information
- Package: com.voicebell.clock
- License: GPL-3.0-or-later
- Website: https://github.com/yourusername/voicebell

## Description
VoiceBell is a privacy-focused alarm clock with offline voice recognition.
All features work without internet connection.

## Checklist
- [x] Metadata follows format
- [x] App is 100% open source (GPL-3.0)
- [x] No proprietary dependencies
- [x] Builds successfully with fdroidserver
- [x] No anti-features
- [x] Changelog is available
```

7. Click "Submit merge request"

## Review Process

### Timeline

- **Initial review**: 1-3 days for basic feedback
- **Full review**: 1-4 weeks depending on reviewer availability
- **Build testing**: F-Droid will test building your app
- **Approval**: Once all issues resolved

### What Reviewers Check

1. **Metadata accuracy**
   - Correct license
   - Accurate description
   - Valid URLs

2. **Build recipe**
   - Builds successfully
   - Reproducible
   - Correct version codes

3. **Source code**
   - Truly open source
   - No proprietary blobs
   - No tracking code

4. **Anti-features**
   - Properly labeled
   - Justified

### Common Feedback

- Missing or incorrect license
- Non-reproducible builds
- Proprietary dependencies found
- Incorrect version codes
- Missing source code for assets
- Unclear anti-features

### Responding to Feedback

1. Address all comments
2. Update metadata or code as needed
3. Push changes to your MR branch
4. Reply to each comment when fixed
5. Mark discussions as resolved

## Updating Your App

### For New Releases

#### Option 1: Automatic Updates (Recommended)

If you set `AutoUpdateMode: Version v%v`:

```yaml
AutoUpdateMode: Version v%v
UpdateCheckMode: Tags
```

F-Droid automatically detects new Git tags and creates builds.

**Process:**
1. Update `versionCode` and `versionName` in `build.gradle`
2. Tag release: `git tag v1.1.0`
3. Push tag: `git push --tags`
4. F-Droid will auto-detect within 24-48 hours

#### Option 2: Manual Updates

Create new merge request to fdroiddata:

```yaml
Builds:
  - versionName: 1.0.0
    versionCode: 1
    commit: v1.0.0
    # ... config

  - versionName: 1.1.0    # New version
    versionCode: 2         # Increment
    commit: v1.1.0         # New tag
    # ... config

CurrentVersion: 1.1.0      # Update
CurrentVersionCode: 2      # Update
```

### Version Code Rules

**Must increase with each release:**

```kotlin
// build.gradle.kts
android {
    defaultConfig {
        versionCode = 2          // Increment by 1
        versionName = "1.1.0"    // Semantic versioning
    }
}
```

**F-Droid won't build if:**
- Version code decreases
- Version code duplicates existing
- Version code doesn't match metadata

## Troubleshooting

### Build Fails: "Unsigned APK"

**Problem**: F-Droid expects unsigned APK

**Solution**: Ensure build produces unsigned APK:
```kotlin
buildTypes {
    release {
        signingConfig = null  // No signing for F-Droid
    }
}
```

### Build Fails: "Scanner found X"

**Problem**: F-Droid scanner detects binaries or proprietary code

**Solution**: Add to metadata:
```yaml
scanignore:
  - app/src/main/assets/models/*.so
```

### Build Fails: "Vosk model not found"

**Problem**: Model not downloaded during prebuild

**Solution**: Add prebuild script:
```yaml
prebuild: |
  mkdir -p src/main/assets/models
  wget https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip
  unzip vosk-model-small-en-us-0.15.zip -d src/main/assets/models/
  rm vosk-model-small-en-us-0.15.zip
```

### Build Fails: "Non-reproducible"

**Problem**: Build output varies

**Solution**:
1. Disable dependency metadata (see [Preparation](#preparation-steps))
2. Pin all dependency versions
3. Remove timestamps from build

### Metadata Rejected: "Description too promotional"

**Problem**: F-Droid prefers factual descriptions

**Bad:**
> "The BEST alarm clock EVER! You'll LOVE it!"

**Good:**
> "Privacy-focused alarm clock with offline voice recognition"

### Long Review Time

**Reasons:**
- Volunteers review in spare time
- Complex apps take longer
- Backlog of submissions

**What to do:**
- Be patient
- Respond promptly to feedback
- Don't spam reviewers

## After Approval

### 1. F-Droid Badge

Add to README.md:

```markdown
[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/com.voicebell.clock/)
```

### 2. Monitor Builds

F-Droid builds your app automatically:

- Check build status: [monitor.f-droid.org](https://monitor.f-droid.org/)
- Review build logs if failures occur
- Fix issues quickly to keep app updated

### 3. User Feedback

Users can comment on F-Droid:

- Monitor F-Droid forums
- Direct users to GitHub Issues for bugs
- Be responsive to community

## Best Practices

1. **Tag frequently**: Release often, F-Droid updates fast
2. **Semantic versioning**: Use MAJOR.MINOR.PATCH format
3. **Detailed changelogs**: Users appreciate knowing what changed
4. **Test builds locally**: Catch issues before F-Droid does
5. **Respond quickly**: Fast responses = faster approvals
6. **Keep metadata updated**: Accurate descriptions help users

## Resources

- **F-Droid documentation**: [f-droid.org/docs](https://f-droid.org/docs/)
- **Metadata reference**: [f-droid.org/docs/Build_Metadata_Reference](https://f-droid.org/docs/Build_Metadata_Reference/)
- **fdroidserver tools**: [gitlab.com/fdroid/fdroidserver](https://gitlab.com/fdroid/fdroidserver)
- **F-Droid forum**: [forum.f-droid.org](https://forum.f-droid.org/)
- **IRC**: #fdroid on OFTC

## Getting Help

- **Forum**: [forum.f-droid.org](https://forum.f-droid.org/)
- **Matrix**: #fdroid:f-droid.org
- **IRC**: #fdroid on irc.oftc.net
- **Email**: team@f-droid.org (for urgent matters only)

---

**Good luck with your F-Droid submission!**
