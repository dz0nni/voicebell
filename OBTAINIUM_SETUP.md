# VoiceBell Obtainium Setup Guide

VoiceBell supports automatic updates through the **Obtainium** app, which downloads APKs directly from GitHub.

## What is Obtainium?

Obtainium is an open-source Android app that allows you to:
- ✅ Install and update apps directly from GitHub
- ✅ Avoid Google Play and F-Droid intermediaries
- ✅ Get updates immediately when they're published
- ✅ Complete privacy and control

## Requirements

- Android 10+ (API 29+)
- Obtainium app installed
- Internet connection (only for downloads)

## 1. Install Obtainium

### Option A: F-Droid (Recommended)
1. Open F-Droid
2. Search for "Obtainium"
3. Install the app

### Option B: GitHub
1. Go to: https://github.com/ImranR98/Obtainium/releases
2. Download the latest `app-release.apk`
3. Install the APK

## 2. Add VoiceBell to Obtainium

### Automatic Method (Recommended)

Click this link on your Android device:

```
obtainium://app/%7B%22id%22%3A%22com.voicebell.clock%22%2C%22url%22%3A%22https%3A%2F%2Fgithub.com%2Fdz0nni%2Fvoicebell%22%2C%22author%22%3A%22dz0nni%22%2C%22name%22%3A%22VoiceBell%22%2C%22additionalSettings%22%3A%22%7B%5C%22trackOnly%5C%22%3Afalse%2C%5C%22includePrereleases%5C%22%3Afalse%2C%5C%22fallbackToOlderReleases%5C%22%3Atrue%2C%5C%22filterReleaseTitlesByRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22filterReleaseNotesByRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22verifyLatestTag%5C%22%3Afalse%2C%5C%22dontSortReleasesList%5C%22%3Afalse%2C%5C%22useLatestAssetDateAsReleaseDate%5C%22%3Afalse%2C%5C%22releaseTitleAsVersion%5C%22%3Afalse%2C%5C%22trackOnlyAssetRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22apkFilterRegEx%5C%22%3A%5C%22debug%5C%5C%5C%5C.apk%24%5C%22%2C%5C%22invertAPKFilter%5C%22%3Afalse%2C%5C%22autoApkFilterByArch%5C%22%3Atrue%2C%5C%22appName%5C%22%3A%5C%22%5C%22%2C%5C%22shizukuPretendToBeGooglePlay%5C%22%3Afalse%2C%5C%22exemptFromBackgroundUpdates%5C%22%3Afalse%2C%5C%22skipUpdateNotifications%5C%22%3Afalse%2C%5C%22about%5C%22%3A%5C%22%5C%22%7D%22%7D
```

**⚠️ Replace `dz0nni` with your GitHub username!**

### Manual Method

1. **Open Obtainium**
2. **Tap the (+) button**
3. **Fill in the following fields:**

   | Field | Value |
   |-------|-------|
   | App Source URL | `https://github.com/dz0nni/voicebell` |
   | App Name | `VoiceBell` |
   | Author | `dz0nni` |

4. **Open "Additional Options":**
   - ✅ Enable `Fallback to Older Releases`
   - ✅ Enable `Auto APK Filter By Architecture`
   - APK Filter RegEx: `debug\.apk$` (debug APKs only)

5. **Tap "Add"**

## 3. Install VoiceBell

1. **In Obtainium, find VoiceBell**
2. **Tap "Download"**
3. **Tap "Install"**
4. **Grant permissions:**
   - Install Unknown Apps (first time only)
   - Microphone (for voice commands)
   - Notifications (Android 13+)

## 4. Automatic Updates

### Configure Automatic Checks

1. Obtainium → **Settings**
2. Enable **"Background Updates"**
3. Set **"Update Check Interval"**: `Once a day` or `Every 12 hours`
4. Enable **"Auto-download Updates"** (optional)
5. Enable **"Auto-install Updates"** (optional)

### Manual Update Check

In Obtainium:
1. Find VoiceBell
2. Tap ↻ (refresh icon)
3. If update available → tap "Download"
4. Tap "Install"

## Versions and Releases

VoiceBell uses semantic versioning:

```
v<MAJOR>.<MINOR>.<PATCH>

Examples:
- v0.1.0 - MVP release (first public version)
- v0.2.0 - New features
- v0.2.1 - Bug fixes
- v1.0.0 - Stable release
```

### Release Types

**Debug APK** (recommended for testing)
- Filename: `VoiceBell-X.X.X-debug.apk`
- Contains debug info
- More flexible security rules
- Larger file size

**Release APK** (future)
- Filename: `VoiceBell-X.X.X-release.apk`
- Optimized (ProGuard)
- Smaller file size
- Requires signing

## Creating GitHub Releases

### If you're a VoiceBell developer:

1. **Ensure all changes are committed:**
   ```bash
   git add .
   git commit -m "Release v0.1.0"
   ```

2. **Create and push tag:**
   ```bash
   git tag v0.1.0
   git push origin v0.1.0
   ```

3. **GitHub Actions:**
   - Automatically compiles APKs
   - Creates GitHub Release
   - Attaches APKs to release
   - Takes 5-10 minutes

4. **Check:**
   - Go to: `https://github.com/dz0nni/voicebell/releases`
   - Should show `v0.1.0` release with APKs

### If you're a user:

- GitHub Actions does everything automatically ✅
- Obtainium detects new releases automatically ✅
- You get notifications about new versions ✅

## Troubleshooting

### ❌ "No releases found"

**Problem:** GitHub repository has no releases

**Solution:**
1. Go to: `https://github.com/dz0nni/voicebell/releases`
2. If empty, you need to create the first tag:
   ```bash
   git tag v0.1.0
   git push origin v0.1.0
   ```

### ❌ "APK not found in release"

**Problem:** Release exists but APK is missing

**Solution:**
1. Check GitHub Actions: `Actions` tab
2. See if `Build and Release APK` workflow succeeded
3. If failed, check logs and fix errors

### ❌ "Installation blocked"

**Problem:** Android blocks installation from unknown source

**Solution:**
1. Settings → Security → Install Unknown Apps
2. Find Obtainium
3. Allow "Allow from this source"

### ❌ "Parse error"

**Problem:** APK is corrupted or wrong architecture

**Solution:**
1. In Obtainium → VoiceBell → Remove
2. Add again and retry
3. Ensure APK Filter is set to: `debug\.apk$`

### ❌ Updates not working

**Problem:** Obtainium doesn't find updates

**Solution:**
1. Obtainium → Settings → Clear Cache
2. Check Background Updates is enabled
3. Manual refresh: ↻ button next to VoiceBell

## Comparison with Other Methods

| Method | Pros | Cons |
|--------|------|------|
| **Obtainium** | ✅ Automatic<br>✅ Private<br>✅ Fast | ⚠️ Requires setup |
| **GitHub Manual** | ✅ Simple<br>✅ Official | ❌ Manual<br>❌ Slow |
| **F-Droid** | ✅ Official<br>✅ Secure | ⏳ Coming soon<br>⏳ Slow review |
| **Google Play** | ✅ Well-known | ⏳ Coming soon<br>❌ Privacy |

## Security

### Obtainium is secure:
- ✅ Open source
- ✅ Downloads directly from GitHub
- ✅ Verifies APK signatures
- ✅ Doesn't track you
- ✅ Doesn't store data

### VoiceBell is secure:
- ✅ Open source (GPL-3.0)
- ✅ 100% offline
- ✅ No internet permission
- ✅ Doesn't track you
- ✅ All data stored locally

### Ensure Secure Download:
1. ✅ URL is `github.com/dz0nni/voicebell`
2. ✅ Releases are signed by GitHub Actions
3. ✅ APK package name is `com.voicebell.clock`
4. ✅ Check SHA256 checksum (optional)

## Next Steps

After installation:

1. **Initial Setup:**
   - Grant necessary permissions
   - Choose UI Mode (Classic or Experimental)
   - Set up your first alarm

2. **Explore Features:**
   - Create alarm with all features
   - Try timer
   - Test stopwatch
   - Add world clock
   - Try voice commands (if Vosk model installed)

3. **Provide Feedback:**
   - GitHub Issues: report bugs or suggest features
   - GitHub Discussions: ask questions
   - GitHub Star: support the project ⭐

## Useful Links

- **VoiceBell GitHub:** https://github.com/dz0nni/voicebell
- **VoiceBell Releases:** https://github.com/dz0nni/voicebell/releases
- **VoiceBell Issues:** https://github.com/dz0nni/voicebell/issues
- **Obtainium GitHub:** https://github.com/ImranR98/Obtainium
- **Obtainium F-Droid:** https://f-droid.org/packages/dev.imranr.obtainium.fdroid

## Help and Support

If you need help:

1. **Read the documentation:**
   - README.md
   - ARCHITECTURE.md
   - TESTING_SUMMARY.md

2. **Search existing issues:**
   - GitHub Issues tab

3. **Create a new issue:**
   - Describe the problem
   - Add screenshots
   - Include your Android version
   - Include VoiceBell version

---

**Enjoy VoiceBell! 🔔**

*Privacy-focused, offline-first, open-source alarm clock for Android.*
