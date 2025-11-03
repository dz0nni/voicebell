# VoiceBell Obtainium Setup Guide

VoiceBell toetab automaatset uuendamist läbi **Obtainium** rakenduse, mis laadib otse GitHubist APK-d.

## Mis on Obtainium?

Obtainium on avatud lähtekoodiga Android rakendus, mis võimaldab:
- ✅ Installida ja uuendada rakendusi otse GitHubist
- ✅ Väldid Google Play ja F-Droid vahendajaid
- ✅ Saada uuendusi kohe kui need avaldatakse
- ✅ Täielik privaatsus ja kontroll

## Nõuded

- Android 10+ (API 29+)
- Obtainium rakendus installitud
- Internetiühendus (ainult allalaadimiseks)

## 1. Installi Obtainium

### Variant A: F-Droid (Soovitatav)
1. Ava F-Droid
2. Otsi "Obtainium"
3. Installi rakendus

### Variant B: GitHub
1. Mine: https://github.com/ImranR98/Obtainium/releases
2. Lae alla viimane `app-release.apk`
3. Installi APK

## 2. Lisa VoiceBell Obtainiumisse

### Automaatne Meetod (Soovitatav)

Klõpsa seda linki oma Android seadmes:

```
obtainium://app/%7B%22id%22%3A%22com.voicebell.clock%22%2C%22url%22%3A%22https%3A%2F%2Fgithub.com%2Fdz0nni%2Fvoicebell%22%2C%22author%22%3A%22dz0nni%22%2C%22name%22%3A%22VoiceBell%22%2C%22additionalSettings%22%3A%22%7B%5C%22trackOnly%5C%22%3Afalse%2C%5C%22includePrereleases%5C%22%3Afalse%2C%5C%22fallbackToOlderReleases%5C%22%3Atrue%2C%5C%22filterReleaseTitlesByRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22filterReleaseNotesByRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22verifyLatestTag%5C%22%3Afalse%2C%5C%22dontSortReleasesList%5C%22%3Afalse%2C%5C%22useLatestAssetDateAsReleaseDate%5C%22%3Afalse%2C%5C%22releaseTitleAsVersion%5C%22%3Afalse%2C%5C%22trackOnlyAssetRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22apkFilterRegEx%5C%22%3A%5C%22debug%5C%5C%5C%5C.apk%24%5C%22%2C%5C%22invertAPKFilter%5C%22%3Afalse%2C%5C%22autoApkFilterByArch%5C%22%3Atrue%2C%5C%22appName%5C%22%3A%5C%22%5C%22%2C%5C%22shizukuPretendToBeGooglePlay%5C%22%3Afalse%2C%5C%22exemptFromBackgroundUpdates%5C%22%3Afalse%2C%5C%22skipUpdateNotifications%5C%22%3Afalse%2C%5C%22about%5C%22%3A%5C%22%5C%22%7D%22%7D
```

**⚠️ Asenda `dz0nni` oma GitHub kasutajanimega!**

### Manuaalne Meetod

1. **Ava Obtainium**
2. **Vajuta (+) nuppu**
3. **Täida järgmised väljad:**

   | Väli | Väärtus |
   |------|---------|
   | App Source URL | `https://github.com/dz0nni/voicebell` |
   | App Name | `VoiceBell` |
   | Author | `dz0nni` |

4. **Ava "Additional Options":**
   - ✅ Enable `Fallback to Older Releases`
   - ✅ Enable `Auto APK Filter By Architecture`
   - APK Filter RegEx: `debug\.apk$` (ainult debug APK-d)

5. **Vajuta "Add"**

## 3. Installi VoiceBell

1. **Obtainiumis leia VoiceBell**
2. **Vajuta "Download"**
3. **Vajuta "Install"**
4. **Anna load:**
   - Install Unknown Apps (esimesel korral)
   - Microphone (voice commands jaoks)
   - Notifications (Android 13+)

## 4. Automaatsed Uuendused

### Seadista Automaatsed Kontrollid

1. Obtainium → **Settings**
2. Enable **"Background Updates"**
3. Määra **"Update Check Interval"**: `Once a day` või `Every 12 hours`
4. Enable **"Auto-download Updates"** (valikuline)
5. Enable **"Auto-install Updates"** (valikuline)

### Manuaalne Uuenduse Kontroll

Obtainiumis:
1. Leia VoiceBell
2. Vajuta ↻ (refresh icon)
3. Kui uuendus saadaval → vajuta "Download"
4. Vajuta "Install"

## Versioonid ja Release'id

VoiceBell kasutab semantilist versioneerimist:

```
v<MAJOR>.<MINOR>.<PATCH>

Näiteks:
- v0.1.0 - MVP release (esimene avalik versioon)
- v0.2.0 - Uued funktsioonid
- v0.2.1 - Bug fixes
- v1.0.0 - Stable release
```

### Release Tüübid

**Debug APK** (soovitatav testimiseks)
- Filename: `VoiceBell-X.X.X-debug.apk`
- Sisaldab debug infot
- Paindlikumad turvareeglid
- Suurem failisuurus

**Release APK** (tulevikus)
- Filename: `VoiceBell-X.X.X-release.apk`
- Optimeeritud (ProGuard)
- Väiksem failisuurus
- Vajalik signing (allkirjastamine)

## GitHub Release'ide Loomine

### Kui oled VoiceBelli arendaja:

1. **Veendu, et kõik muudatused on committed:**
   ```bash
   git add .
   git commit -m "Release v0.1.0"
   ```

2. **Loo ja push tag:**
   ```bash
   git tag v0.1.0
   git push origin v0.1.0
   ```

3. **GitHub Actions:**
   - Automaatselt kompileerib APK-d
   - Loob GitHub Release'i
   - Lisab APK-d release'i külge
   - Võtab 5-10 minutit

4. **Kontrolli:**
   - Mine: `https://github.com/dz0nni/voicebell/releases`
   - Peaks näitama `v0.1.0` release'i koos APK-dega

### Kui oled kasutaja:

- GitHub Actions teeb kõik automaatselt ✅
- Obtainium tuvastab uue release'i automaatselt ✅
- Sa saad notifikatiooni uue versiooni kohta ✅

## Troubleshooting

### ❌ "No releases found"

**Probleem:** GitHub repositooryl pole ühtegi release'i

**Lahendus:**
1. Ava: `https://github.com/dz0nni/voicebell/releases`
2. Kui tühi, siis pead looma esimese tag'i:
   ```bash
   git tag v0.1.0
   git push origin v0.1.0
   ```

### ❌ "APK not found in release"

**Probleem:** Release eksisteerib, aga APK puudub

**Lahendus:**
1. Kontrolli GitHub Actions: `Actions` tab
2. Vaata kas `Build and Release APK` workflow õnnestus
3. Kui ebaõnnestus, vaata logisid ja paranda vead

### ❌ "Installation blocked"

**Probleem:** Android blokeerib unknown source'ist installimise

**Lahendus:**
1. Settings → Security → Install Unknown Apps
2. Leia Obtainium
3. Luba "Allow from this source"

### ❌ "Parse error"

**Probleem:** APK on kahjustatud või vale arhitektuur

**Lahendus:**
1. Obtainiumis → VoiceBell → Remove
2. Lisa uuesti ja proovi uuesti
3. Veendu, et APK Filter on seatud: `debug\.apk$`

### ❌ Updates not working

**Probleem:** Obtainium ei leia uuendusi

**Lahendus:**
1. Obtainium → Settings → Clear Cache
2. Kontrolli Background Updates on enabled
3. Käsitsi refresh: ↻ nupp VoiceBelli juures

## Võrdlus Teiste Meetoditega

| Meetod | Plussid | Miinused |
|--------|---------|----------|
| **Obtainium** | ✅ Automaatne<br>✅ Privaatne<br>✅ Kiire | ⚠️ Vajab seadistamist |
| **GitHub Manual** | ✅ Lihtne<br>✅ Ametlik | ❌ Manuaalne<br>❌ Aeglane |
| **F-Droid** | ✅ Ametlik<br>✅ Turvaline | ⏳ Tuleb tulevikus<br>⏳ Aeglane review |
| **Google Play** | ✅ Tuntud | ⏳ Tuleb tulevikus<br>❌ Privaatsus |

## Turvalisus

### Obtainium on turvaline:
- ✅ Avatud lähtekoodiga
- ✅ Laadib otse GitHubist
- ✅ Kontrollib APK signatuuri
- ✅ Ei jälgi sind
- ✅ Ei salvesta andmeid

### VoiceBell on turvaline:
- ✅ Avatud lähtekoodiga (GPL-3.0)
- ✅ 100% offline
- ✅ Ei kasuta interneti luba
- ✅ Ei jälgi sind
- ✅ Kõik andmed lokaalselt

### Veendu Turvalises Allalaadimises:
1. ✅ URL on `github.com/dz0nni/voicebell`
2. ✅ Release'id on signed by GitHub Actions
3. ✅ APK package name on `com.voicebell.clock`
4. ✅ Kontrolli SHA256 checksummi (valikuline)

## Järgmised Sammud

Pärast installimist:

1. **Esmakordne Seadistamine:**
   - Anna vajalikud load
   - Vali UI Mode (Classic või Experimental)
   - Seadista esimene äratus

2. **Tutvu Funktsioonidega:**
   - Loo äratus kõikide funktsioonidega
   - Proovi timerit
   - Testi stopwatch'i
   - Lisa world clock
   - Proovi voice command'i (kui Vosk mudel olemas)

3. **Anna Tagasisidet:**
   - GitHub Issues: raportreeri bugi või soovita funktsiooni
   - GitHub Discussions: küsi küsimusi
   - GitHub Star: toeta projekti ⭐

## Kasulikud Lingid

- **VoiceBell GitHub:** https://github.com/dz0nni/voicebell
- **VoiceBell Releases:** https://github.com/dz0nni/voicebell/releases
- **VoiceBell Issues:** https://github.com/dz0nni/voicebell/issues
- **Obtainium GitHub:** https://github.com/ImranR98/Obtainium
- **Obtainium F-Droid:** https://f-droid.org/packages/dev.imranr.obtainium.fdroid

## Abi ja Tugi

Kui vajad abi:

1. **Loe dokumentatsiooni:**
   - README.md
   - ARCHITECTURE.md
   - TESTING_SUMMARY.md

2. **Otsi olemasolevaid issue'sid:**
   - GitHub Issues tab

3. **Loo uus issue:**
   - Kirjelda probleem
   - Lisa ekraanitõmmised
   - Märgi oma Android versioon
   - Märgi VoiceBelli versioon

---

**Nauди VoiceBelli! 🔔**

*Privacy-focused, offline-first, open-source alarm clock for Android.*
