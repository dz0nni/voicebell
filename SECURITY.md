# Security Policy

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 0.1.x   | :white_check_mark: |

We recommend always using the latest version for the best security and features.

## Security Commitment

VoiceBell is designed with security and privacy as top priorities:

- **No network access**: The app has NO internet permission
- **Local data only**: All data stored on device in app-private storage
- **No tracking**: Zero analytics, telemetry, or data collection
- **Open source**: Fully auditable code under GPL-3.0
- **Offline voice**: Voice recognition uses Vosk (no cloud processing)

## Reporting a Vulnerability

We take security seriously. If you discover a security vulnerability in VoiceBell, please report it responsibly.

### What to Report

Report any issues that could compromise:
- User privacy
- Data security
- Device security
- App integrity

**Examples:**
- Data leakage to other apps
- Privilege escalation
- Code injection vulnerabilities
- Insecure data storage
- Cryptographic weaknesses

### How to Report

**For security vulnerabilities, please DO NOT open a public GitHub issue.**

Instead, report privately via:

1. **Email**: [your-security-email@example.com]
   - Subject: `[SECURITY] VoiceBell Vulnerability Report`
   - Include: Detailed description, steps to reproduce, impact assessment

2. **GitHub Security Advisory** (Preferred):
   - Go to: https://github.com/yourusername/voicebell/security/advisories
   - Click "Report a vulnerability"
   - Fill in the advisory form

### What to Include

Please provide:

1. **Description**: Clear explanation of the vulnerability
2. **Impact**: What could an attacker do?
3. **Steps to reproduce**: Detailed reproduction steps
4. **Affected versions**: Which versions are vulnerable?
5. **Proof of concept**: Code, screenshots, or demo (if applicable)
6. **Suggested fix**: If you have ideas (optional)

**Template:**
```markdown
## Vulnerability Description
[Brief description]

## Impact
[What could happen if exploited?]

## Steps to Reproduce
1. [First step]
2. [Second step]
3. [...]

## Affected Versions
- VoiceBell version: X.X.X
- Android version: X.X
- Device: [model]

## Proof of Concept
[Code, logs, screenshots]

## Suggested Fix
[Your recommendations]
```

### Response Timeline

We will:

1. **Acknowledge** your report within **48 hours**
2. **Investigate** and validate the issue within **7 days**
3. **Provide updates** every 7 days until resolved
4. **Fix** the vulnerability in a patch release
5. **Credit** you in the release notes (if desired)

### Disclosure Policy

- **Coordinated disclosure**: We prefer 90 days between report and public disclosure
- **Early disclosure**: If vulnerability is critical and actively exploited, we may disclose sooner
- **Credit**: We'll credit researchers in release notes (unless you prefer anonymity)

### What to Expect

**If vulnerability is valid:**
1. We'll acknowledge and thank you
2. Work on a fix
3. Release a security patch
4. Publish a security advisory
5. Credit you (if desired)

**If vulnerability is not valid:**
1. We'll explain why
2. Suggest alternative reporting if it's a bug, not a security issue

## Security Best Practices for Users

### Protect Your Device

1. **Use device lock**: PIN, password, or biometric
2. **Keep Android updated**: Install security patches
3. **Download from trusted sources**: F-Droid, GitHub releases, Google Play
4. **Review permissions**: VoiceBell only needs necessary permissions
5. **Verify checksums**: Check APK SHA-256 before installing

### Data Security

- **Backup encryption**: Android backups are encrypted (if enabled)
- **Local storage**: All alarm data is in app-private directory
- **No cloud**: Data never leaves your device
- **Uninstall removes data**: All data deleted when app is uninstalled

### Voice Recognition Privacy

- **Offline processing**: Vosk runs entirely on-device
- **No recording**: Audio is processed in real-time, never saved
- **Microphone control**: Only active when you press voice button
- **Revoke permission**: You can disable microphone in Android settings

## Known Limitations

### Not Security Issues

The following are expected behavior and not vulnerabilities:

1. **Android backup**: If you enable Android backup to Google Drive, alarm data is included in encrypted backup (user choice)
2. **Root access**: Users with root access can access app's private data (inherent to Android rooting)
3. **Screen recording**: System screen recording can capture alarm times (Android OS feature)
4. **Accessibility services**: Malicious accessibility services could read UI content (Android OS limitation)

## Security Updates

Security patches will be released as:

- **Critical**: Immediate patch release (within 24-48 hours)
- **High**: Patch within 7 days
- **Medium**: Included in next scheduled release
- **Low**: Included in next minor version

Updates are published:
- GitHub Releases
- F-Droid (24-48 hour delay)
- Google Play (24-48 hour review)

## Security Audit

VoiceBell has not yet undergone a professional security audit. We welcome:

- Community code reviews
- Security research
- Penetration testing
- Vulnerability disclosure

## Cryptography

**Current state**: VoiceBell does not use encryption for local data storage.

**Future consideration**: We may add SQLCipher for database encryption in a future release.

## Dependencies

VoiceBell's dependencies are open source and regularly updated:

- **Vosk** (speech recognition): Apache 2.0 license
- **Jetpack Compose**: Apache 2.0 license
- **Room**: Apache 2.0 license
- **Kotlin**: Apache 2.0 license

We monitor security advisories for all dependencies.

## Threat Model

### In Scope

- Vulnerabilities in VoiceBell code
- Insecure data storage
- Privacy leaks
- Permission abuse
- Dependency vulnerabilities

### Out of Scope

- Android OS vulnerabilities
- Physical device access
- Social engineering
- Rooted/jailbroken devices (user's responsibility)
- Malware on device
- Third-party app store malware

## Bug Bounty

We currently **do not** offer a bug bounty program.

However, we deeply appreciate security research and will:
- Publicly credit researchers (if desired)
- Provide recognition in release notes
- Respond promptly to reports
- Work with you to understand and fix issues

## Contact

**Security issues**: [your-security-email@example.com]

**General questions**: [GitHub Issues](https://github.com/yourusername/voicebell/issues)

**PGP Key**: [Optional: Include PGP key fingerprint]

---

**Thank you for helping keep VoiceBell secure!**

*Last updated: 2025-01-01*
