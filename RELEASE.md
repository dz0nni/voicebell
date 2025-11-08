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
   - Keystore Password: **[PRIVATE - Not in Git]**
   - Key Password: **[PRIVATE - Not in Git]**
   - **CRITICAL:** Never commit the keystore to Git!
   - **CRITICAL:** Never commit passwords to Git!
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

**⚠️ SECURITY WARNING:** Never use `--ks-pass pass:"password"` in command line!
- Passwords appear in shell history (`.bash_history`, `.zsh_history`)
- Passwords visible in process list (`ps aux`)
- May be logged to system logs

#### Method 1: Interactive Prompt (RECOMMENDED)

```bash
# Set Java environment
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

# Sign APK - will prompt for password
~/Library/Android/sdk/build-tools/34.0.0/apksigner sign \
  --ks voicebell-release.keystore \
  --ks-key-alias voicebell \
  --out VoiceBell-0.1.10.apk \
  app/build/outputs/apk/release/app-release-unsigned.apk

# You will be prompted:
# Keystore password for signer #1:
# (enter your keystore password)
```

#### Method 2: Environment Variable

```bash
# Set passwords in environment (won't appear in history)
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export KS_PASS="[YOUR_KEYSTORE_PASSWORD]"
export KEY_PASS="[YOUR_KEY_PASSWORD]"

# Sign APK using environment variables
~/Library/Android/sdk/build-tools/34.0.0/apksigner sign \
  --ks voicebell-release.keystore \
  --ks-key-alias voicebell \
  --ks-pass env:KS_PASS \
  --key-pass env:KEY_PASS \
  --out VoiceBell-0.1.10.apk \
  app/build/outputs/apk/release/app-release-unsigned.apk

# Clear passwords from environment
unset KS_PASS KEY_PASS
```

#### Method 3: Secure Script (Best for Automation)

Create `sign-release.sh`:

```bash
#!/bin/bash
set -e

# Prompt for password securely (won't show in terminal)
read -s -p "Keystore password: " KEYSTORE_PASSWORD
echo ""

export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

~/Library/Android/sdk/build-tools/34.0.0/apksigner sign \
  --ks voicebell-release.keystore \
  --ks-key-alias voicebell \
  --ks-pass env:KEYSTORE_PASSWORD \
  --key-pass env:KEYSTORE_PASSWORD \
  --out "$1" \
  app/build/outputs/apk/release/app-release-unsigned.apk

# Clear password from memory
unset KEYSTORE_PASSWORD

echo "APK signed successfully: $1"
```

Make executable and use:

```bash
chmod +x sign-release.sh
./sign-release.sh VoiceBell-0.1.10.apk
```

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

**Current passwords:**
- Keystore Password: **[PRIVATE - Stored securely offline]**
- Key Password: **[PRIVATE - Stored securely offline]**

**Best practices:**
- **NEVER** commit passwords to Git
- **NEVER** share passwords in public channels
- Store passwords in password manager or encrypted storage
- Use environment variables in scripts (never hardcode)
- Keep encrypted backups of passwords
- Keystore and key passwords are different for enhanced security

### GitHub Secrets (for CI/CD)

If setting up automated builds:

1. Convert keystore to Base64:
   ```bash
   base64 -i voicebell-release.keystore -o keystore.b64
   ```

2. Add GitHub secrets:
   - `KEYSTORE_BASE64`: content of keystore.b64
   - `KEYSTORE_PASSWORD`: [your keystore password]
   - `KEY_PASSWORD`: [your key password]
   - `KEY_ALIAS`: `voicebell`

3. Update `.github/workflows/release.yml` to use secrets

## Quick Reference

### Using sign-release.sh script (RECOMMENDED)

```bash
# 1. Build
./gradlew clean assembleRelease

# 2. Sign (will prompt for password)
./sign-release.sh VoiceBell-$(grep versionName app/build.gradle.kts | sed 's/.*"\(.*\)".*/\1/').apk

# 3. Test & Release
adb install -r VoiceBell-*.apk
git commit -m "release: version X.Y.Z"
gh release create vX.Y.Z --title "..." --notes "..." VoiceBell-*.apk
```

### Manual process (if script not available)

```bash
# Build
./gradlew clean assembleRelease

# Sign (interactive - will prompt for password)
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
~/Library/Android/sdk/build-tools/34.0.0/apksigner sign \
  --ks voicebell-release.keystore \
  --ks-key-alias voicebell \
  --out VoiceBell-0.1.10.apk \
  app/build/outputs/apk/release/app-release-unsigned.apk
# (Enter keystore password when prompted)
```

## Additional Resources

- [Android App Signing](https://developer.android.com/studio/publish/app-signing)
- [Android Release Workflow](https://developer.android.com/studio/publish)
- [Semantic Versioning](https://semver.org/)
- [Keep a Changelog](https://keepachangelog.com/)

---

**Last Updated:** 2025-11-08
**Maintainer:** dzOnni <dz0nni@users.noreply.github.com>
