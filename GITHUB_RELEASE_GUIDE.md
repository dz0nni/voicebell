# GitHub Release Quick Start Guide

Juhend VoiceBelli GitHubi üleslaadimiseks ja esimese release'i loomiseks.

## Eeldused

- ✅ Git installitud
- ✅ GitHub konto
- ✅ Projekt selles kaustas valmis

## Samm 1: Loo GitHub Repository

### Veebis (Soovitatud):

1. Mine: https://github.com/new
2. Täida väljad:
   - **Repository name:** `voicebell`
   - **Description:** `Privacy-focused offline alarm clock for Android with voice commands`
   - **Visibility:** Public ✅ (Obtainiumiga vajalik)
   - ❌ **ÄRJA** lisa README, .gitignore ega LICENSE (juba olemas)
3. Vajuta **"Create repository"**

### Käsurealt:

```bash
# Loo GitHub CLI abil (kui installitud)
gh repo create voicebell --public --source=. --remote=origin
```

## Samm 2: Lisa Git Remote ja Push Kood

```bash
# Kontrolli, et oled projektikaustas
cd /docker/tannu/claude/clock

# Initsiiseeri Git (kui pole juba)
git init

# Lisa kõik failid
git add .

# Tee esimene commit
git commit -m "Initial commit - VoiceBell v0.1.0 MVP

- Complete alarm system with pre-alarms, snooze, flash
- Timer with countdown and notifications
- Stopwatch with lap recording
- World clocks with 15 cities
- Voice recognition with Vosk
- Dual UI modes (Classic & Experimental)
- Material Design 3
- 100% offline, privacy-focused
- GPL-3.0 license"

# Lisa GitHub remote (ASENDA dz0nni!)
git remote add origin https://github.com/dz0nni/voicebell.git

# Push main branch
git push -u origin main
```

## Samm 3: Loo Esimene Release

### Meetod A: Käsurealt (Kiire)

```bash
# Loo version tag
git tag -a v0.1.0 -m "Release v0.1.0 - MVP

VoiceBell first public release!

Features:
- Alarm system with pre-alarms, snooze, gradual volume, flash
- Timer with countdown notifications
- Stopwatch with lap recording
- World clocks (15 cities)
- Voice commands (Vosk integration)
- Dual UI modes
- Material Design 3
- 100% offline, no tracking

Known limitations:
- Vosk model not included (40MB)
- No alarm tone picker
- Android 10+ only

Full details in TESTING_SUMMARY.md"

# Push tag (käivitab GitHub Actions)
git push origin v0.1.0
```

### Meetod B: GitHub Veebis (Üksikasjalik)

1. **Mine:** `https://github.com/dz0nni/voicebell/releases`
2. **Vajuta:** "Create a new release"
3. **Täida:**
   - **Tag:** `v0.1.0` (loo uus tag)
   - **Release title:** `VoiceBell v0.1.0 - MVP Release`
   - **Description:**
     ```markdown
     # VoiceBell v0.1.0 - First Public Release 🎉

     Privacy-focused offline alarm clock for Android with voice commands.

     ## ✨ Features

     ### Alarm System
     - ⏰ Pre-alarms (1-10, default 7 minutes)
     - 🔁 Snooze (configurable duration and count)
     - 📈 Gradual volume increase (20 steps over 60 seconds)
     - 📱 Vibration per alarm
     - 💡 Flash (camera LED) per alarm
     - 📅 Repeat days selector
     - 🔄 Boot persistence

     ### Timer
     - ⏱️ Countdown with notifications
     - ⏸️ Pause/Resume/Stop
     - 📊 Progress bar in notification
     - 🔄 Recent timers with restart

     ### Stopwatch
     - ▶️ Start/Pause/Resume/Reset
     - 🏁 Lap recording
     - ⚡ Real-time updates (10ms)

     ### World Clocks
     - 🌍 15 pre-configured cities
     - 🕐 Real-time updating
     - 🌐 Timezone offset display

     ### Voice Recognition
     - 🎤 Offline with Vosk
     - 🗣️ "Set alarm for 7 AM"
     - ⏲️ "Timer for 5 minutes"

     ### UI & Design
     - 🎨 Material Design 3
     - 🔀 Dual UI modes (Classic & Experimental)
     - 🌓 Dark mode support
     - 📱 Modern Android UX

     ## 📱 Installation

     ### Obtainium (Recommended)
     See [OBTAINIUM_SETUP.md](OBTAINIUM_SETUP.md) for automatic updates.

     ### Manual Download
     1. Download `VoiceBell-0.1.0-debug.apk` below
     2. Install on Android 10+
     3. Grant permissions (Microphone, Notifications)

     ## ⚠️ Known Limitations

     - Vosk model not included (voice commands require manual setup)
     - No alarm tone picker (uses system default)
     - Android 10+ only
     - Debug build (larger file size)

     ## 📖 Documentation

     - [README.md](README.md) - Overview
     - [ARCHITECTURE.md](ARCHITECTURE.md) - Technical details
     - [TESTING_SUMMARY.md](TESTING_SUMMARY.md) - Testing status
     - [OBTAINIUM_SETUP.md](OBTAINIUM_SETUP.md) - Auto-updates

     ## 🔒 Privacy

     - ✅ 100% offline
     - ✅ No internet permission
     - ✅ No tracking
     - ✅ Open source (GPL-3.0)
     - ✅ All data stored locally

     ## 🙏 Acknowledgments

     Built with:
     - Kotlin & Jetpack Compose
     - Material Design 3
     - Vosk (offline speech recognition)
     - Room Database
     - Hilt (Dependency Injection)

     ## 📝 Changelog

     Initial MVP release with all core features.

     ---

     **Full changelog:** https://github.com/dz0nni/voicebell/commits/v0.1.0
     ```

4. **Välju:** Public (mitte Draft)
5. **Vajuta:** "Publish release"

## Samm 4: Oota GitHub Actions

1. **Mine:** `https://github.com/dz0nni/voicebell/actions`
2. **Vaata:** "Build and Release APK" workflow
3. **Oota:** ~5-10 minutit
4. **Kontrolli:** Peaks muutuma roheliseks ✅

Kui valmis:
- Release'is on nüüd APK failid lisatud
- `VoiceBell-0.1.0-debug.apk` on allalaadimiseks valmis

## Samm 5: Testi Release'i

```bash
# Lae alla release APK (ASENDA URL!)
wget https://github.com/dz0nni/voicebell/releases/download/v0.1.0/VoiceBell-0.1.0-debug.apk

# Või käsitsi:
# - Mine releases lehele
# - Vajuta APK failil
# - Salvesta oma seadmesse
```

## Järgmised Release'id

Kui teed muudatusi ja tahad uut release'i:

```bash
# 1. Tee muudatused
git add .
git commit -m "Fix: description of changes"
git push

# 2. Uuenda versiooni build.gradle.kts failis:
# versionName = "0.1.1" (oli 0.1.0)

# 3. Loo uus tag
git tag -a v0.1.1 -m "Release v0.1.1 - Bug fixes

- Fix alarm snooze issue
- Improve timer notifications
- Update documentation"

# 4. Push tag
git push origin v0.1.1
```

## Probleemide Lahendamine

### ❌ GitHub Actions ebaõnnestub

**Vaata logi:**
1. Actions tab → Klõpsa workflow'l
2. Vaata punast sammu
3. Loe error message

**Levinud vead:**

**1. "gradlew permission denied"**
```bash
# Kohapeale:
chmod +x gradlew
git add gradlew
git commit -m "Fix: make gradlew executable"
git push origin main

# Loo tag uuesti:
git tag -d v0.1.0
git push origin :refs/tags/v0.1.0
git tag v0.1.0
git push origin v0.1.0
```

**2. "Gradle wrapper not found"**
- Veendu, et `gradle/wrapper/` kaust on committitud:
```bash
git add gradle/wrapper/
git commit -m "Add Gradle wrapper"
git push
```

**3. "Build failed"**
- Kompileerimisvead koodis
- Vaata Actions logi detaile
- Paranda ja push uuesti

### ❌ APK puudub Release'is

- Workflow käis läbi, aga ei lisanud APK'd
- Kontrolli workflow YAML faili
- Veendu, et `softprops/action-gh-release` samm õnnestus

### ❌ Clone ebaõnnestub

```bash
# Kui git clone ei tööta:
git remote -v  # Kontrolli URL
git remote set-url origin https://github.com/dz0nni/voicebell.git
```

## Lisafunktsioonid

### Automatic Build on Every Push

Olemas: `.github/workflows/build.yml`
- Kompileerib APK iga push'i peale
- APK saadaval Artifacts'ides
- Pole release, aga hea testimiseks

### Pre-releases (Beta)

Kui tahad beta release'i:
```bash
git tag -a v0.2.0-beta -m "Beta release for testing"
git push origin v0.2.0-beta
```

GitHub'is märgi release kui "Pre-release" ✅

### Signed Release APK

Tulevikus production release'ideks:
1. Genereeri keystore
2. Lisa GitHub Secrets:
   - `KEYSTORE_FILE` (base64 encoded)
   - `KEYSTORE_PASSWORD`
   - `KEY_ALIAS`
   - `KEY_PASSWORD`
3. Uuenda workflow signed build'iga

## Obtainium Seadistamine

Pärast esimese release'i loomist:

1. **Kasutajad saavad lisada Obtainiumisse:**
   - URL: `https://github.com/dz0nni/voicebell`
   - APK Filter: `debug\.apk$`

2. **Saada neile OBTAINIUM_SETUP.md link**

3. **Iga uus release:**
   - Push tag → GitHub Actions build → Release valmis
   - Obtainium tuvastab automaatselt ✅
   - Kasutajad saavad notifikatsiooni ✅

## Checklist

Enne esimest release'i:

- [ ] GitHub repo loodud
- [ ] Kood pushed GitHubi
- [ ] `gradlew` on executable
- [ ] `.github/workflows/release.yml` olemas
- [ ] Version number on õige (`build.gradle.kts`)
- [ ] Tag loodud ja pushed
- [ ] GitHub Actions käib
- [ ] Release ilmub (5-10 min)
- [ ] APK on release'is
- [ ] APK installeeritav
- [ ] OBTAINIUM_SETUP.md on updated (dz0nni)

## Näited

**Hea Release Title:**
```
VoiceBell v0.1.0 - MVP Release
```

**Hea Release Description:**
- Selge feature nimekiri
- Teadaolevad piirangud
- Installeerimisjuhend
- Lingid dokumentatsioonile

**Hea Commit Message:**
```
Release v0.1.0 - MVP

- Complete alarm system
- Timer and stopwatch
- World clocks
- Voice recognition
- Dual UI modes
```

**Hea Tag Message:**
```
Release v0.1.0 - MVP

First public release with all core features.
See CHANGELOG.md for details.
```

---

**Õnne projekti käivitamisega! 🚀**

Kui midagi on ebaselge, vaata:
- README.md
- TESTING_SUMMARY.md
- GitHub Actions dokumentatsioon
