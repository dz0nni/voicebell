# Release Process

This document describes how to create and publish new releases of VoiceBell.

## Prerequisites

1. **Java Development Kit (JDK)**
   - Android Studio includes JDK at: `/Applications/Android Studio.app/Contents/jbr/Contents/Home`
   - Set `JAVA_HOME`: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`

2. **Android SDK Build Tools**
   - Install via Android Studio SDK Manager
   - Or via command line: `sdkmanager "build-tools;34.0.0"`

3. **Release Keystore**
   - Location: `voicebell-release.keystore` (in project root, ignored by Git)
   - Alias: `voicebell`
   - Password: `VoiceBell2025!Secure` (store securely!)
   - **CRITICAL:** Never commit the keystore to Git!
   - **CRITICAL:** Back up the keystore securely!
   - **CRITICAL:** All releases must use the same keystore!

## Keystore Information

### Current Release Keystore Details

```
File: voicebell-release.keystore
Alias: voicebell
Key Algorithm: RSA
Key Size: 2048 bits
Validity: 10,000 days (~27 years)
DN: CN=VoiceBell, OU=Development, O=VoiceBell, L=Tallinn, ST=Harjumaa, C=EE
Created: 2025-11-08
```

### Why Keystore Security Matters

- Android requires all app updates to be signed with the **same key**
- If you lose the keystore, you **cannot update the app** on user devices
- Users would need to **uninstall and reinstall** (losing all data)
- **Keep multiple backups** in secure locations

### Backup Recommendations

1. **Encrypted USB drive** (stored securely offline)
2. **Encrypted cloud storage** (e.g., password-protected 7zip archive)
3. **Password manager** with secure attachment storage
4. **Document the password** in a secure location

## Release Steps

### 1. Update Version Numbers

Edit `app/build.gradle.kts`:

```kotlin
versionCode = 11  // Increment by 1
versionName = "0.1.10"  // Follow semver
```

### 2. Update CHANGELOG.md

Add release notes under `## [Unreleased]` section:

```markdown
## [0.1.10] - 2025-11-XX

### Added
- New feature description

### Fixed
- Bug fix description

### Changed
- Change description
```

### 3. Build Release APK

```bash
# Clean previous builds
./gradlew clean

# Build release APK (unsigned)
./gradlew assembleRelease

# Output: app/build/outputs/apk/release/app-release-unsigned.apk
```

### 4. Sign APK with Release Keystore

```bash
# Set Java environment
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

# Sign APK
~/Library/Android/sdk/build-tools/34.0.0/apksigner sign \
  --ks voicebell-release.keystore \
  --ks-key-alias voicebell \
  --ks-pass pass:"VoiceBell2025!Secure" \
  --key-pass pass:"VoiceBell2025!Secure" \
  --out VoiceBell-0.1.10.apk \
  app/build/outputs/apk/release/app-release-unsigned.apk
```

**IMPORTANT:** Never put passwords in shell history! Use one of these methods:
- Store password in environment variable: `--ks-pass env:KEYSTORE_PASS`
- Use interactive prompt: `--ks-pass stdin` (then type password)
- Or use a secure script

### 5. Verify APK Signature

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

~/Library/Android/sdk/build-tools/34.0.0/apksigner verify \
  --print-certs \
  VoiceBell-0.1.10.apk
```

Expected output should include:
```
Signer #1 certificate DN: CN=VoiceBell, OU=Development, O=VoiceBell, L=Tallinn, ST=Harjumaa, C=EE
```

### 6. Test APK Locally

```bash
# Install on connected device/emulator
adb install VoiceBell-0.1.10.apk

# Or update existing installation
adb install -r VoiceBell-0.1.10.apk
```

**Test thoroughly:**
- App launches successfully
- All features work as expected
- Voice recognition functions properly
- No crashes or errors

### 7. Commit Changes

```bash
git add app/build.gradle.kts CHANGELOG.md
git commit -m "release: version 0.1.10 - [Brief description]

[Detailed release notes]

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>"
```

**Git author configuration:**
- Name: `dzOnni`
- Email: `dz0nni@users.noreply.github.com`

### 8. Push and Create GitHub Release

```bash
# Push commit
git push

# Create GitHub release with APK
gh release create v0.1.10 \
  --title "v0.1.10 - [Release Title]" \
  --notes-file release-notes.md \
  VoiceBell-0.1.10.apk
```

Or use inline notes:

```bash
gh release create v0.1.10 \
  --title "v0.1.10 - [Release Title]" \
  --notes "Release notes here..." \
  VoiceBell-0.1.10.apk
```

### 9. Verify Obtainium Compatibility

Test that Obtainium can:
1. Detect the new release
2. Download the APK
3. Install/update successfully

## Troubleshooting

### APK Signature Mismatch

**Error:** "Package signatures do not match the previously installed version"

**Cause:** Different keystore was used

**Solution:**
- Ensure you're using `voicebell-release.keystore`
- Check keystore alias is `voicebell`
- Verify password is correct
- If testing, uninstall old version first: `adb uninstall com.voicebell.clock`

### Keystore Lost or Forgotten Password

**Prevention is critical!** If keystore is lost:

1. You **cannot update** existing installations
2. You must create a **new app** with a different package name
3. Users must **uninstall and reinstall**

**Recovery steps if keystore is lost:**
1. Create new keystore (different alias/password)
2. Change package name in `build.gradle.kts`
3. Publish as completely new app
4. Notify users to uninstall old version

### APK Installation Fails

**Error:** "INSTALL_PARSE_FAILED_NO_CERTIFICATES"

**Cause:** APK is not signed

**Solution:** Follow step 4 to sign the APK

## Version History

### v0.1.9 (Current)
- **Keystore:** voicebell-release.keystore (release keystore, created 2025-11-08)
- **Signature:** `[signature hash will be added]`
- First release with production keystore

### v0.1.8 (Previous)
- **Keystore:** [different keystore - incompatible]
- **Note:** Cannot update from v0.1.8 to v0.1.9 without uninstall

### v0.1.7 and earlier
- Various keystores used during development
- Not compatible with v0.1.9+

## Security Notes

### Keystore Password Management

**Current password:** `VoiceBell2025!Secure`

**Best practices:**
- Never commit password to Git
- Never share password in public channels
- Consider using environment variables in scripts
- Rotate password periodically (regenerate keystore if needed)

### GitHub Secrets (for CI/CD)

If setting up automated builds:

1. Convert keystore to Base64:
   ```bash
   base64 -i voicebell-release.keystore -o keystore.b64
   ```

2. Add GitHub secrets:
   - `KEYSTORE_BASE64`: content of keystore.b64
   - `KEYSTORE_PASSWORD`: `VoiceBell2025!Secure`
   - `KEY_ALIAS`: `voicebell`

3. Update `.github/workflows/release.yml` to use secrets

## Quick Reference

```bash
# Complete release process (one-liner)
./gradlew clean assembleRelease && \
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && \
~/Library/Android/sdk/build-tools/34.0.0/apksigner sign \
  --ks voicebell-release.keystore \
  --ks-key-alias voicebell \
  --ks-pass pass:"VoiceBell2025!Secure" \
  --key-pass pass:"VoiceBell2025!Secure" \
  --out VoiceBell-$(grep versionName app/build.gradle.kts | sed 's/.*"\(.*\)".*/\1/').apk \
  app/build/outputs/apk/release/app-release-unsigned.apk
```

## Additional Resources

- [Android App Signing](https://developer.android.com/studio/publish/app-signing)
- [Android Release Workflow](https://developer.android.com/studio/publish)
- [Semantic Versioning](https://semver.org/)
- [Keep a Changelog](https://keepachangelog.com/)

---

**Last Updated:** 2025-11-08
**Maintainer:** dzOnni <dz0nni@users.noreply.github.com>
