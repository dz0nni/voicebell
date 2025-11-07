# VoiceBell Quick Start Guide 🚀

**Kiirete sammudega GitHubi ja Obtainiumini!**

## ⚡ Kiire Ülevaade

1. ✅ Kood on valmis
2. ✅ GitHub Actions seadistatud
3. ✅ Obtainium tugi lisatud
4. 📤 Push GitHubi → Automaatne APK build
5. 📱 Installeeri Obtainiumiga

---

## 📋 Sammud

### 1️⃣ Loo GitHub Repository (2 min)

**Veebis:**
```
1. Mine: https://github.com/new
2. Repo nimi: voicebell
3. Public ✅
4. ❌ ÄRA lisa README/gitignore/LICENSE (juba olemas)
5. Create repository
```

**Või CLI'st:**
```bash
gh repo create voicebell --public --source=. --remote=origin
```

### 2️⃣ Push Kood GitHubi (1 min)

```bash
# Projekti kaustas
cd /docker/tannu/claude/clock

# Initsiiseeri git (kui vaja)
git init
git branch -M main

# Lisa failid
git add .
git commit -m "Initial commit - VoiceBell v0.1.0 MVP"

# Lisa remote (ASENDA dz0nni!)
git remote add origin https://github.com/dz0nni/voicebell.git

# Push
git push -u origin main
```

### 3️⃣ Loo Esimene Release (30 sec)

```bash
# Loo tag
git tag -a v0.1.0 -m "Release v0.1.0 - MVP"

# Push tag (käivitab GitHub Actions)
git push origin v0.1.0
```

### 4️⃣ Oota GitHub Actions (5-10 min)

```
1. Mine: https://github.com/dz0nni/voicebell/actions
2. Vaata "Build and Release APK" workflow
3. Oota kuni roheline ✅
4. Kontrolli: https://github.com/dz0nni/voicebell/releases
```

### 5️⃣ Installi Obtainiumiga (2 min)

**Obtainium seadistus:**
```
1. Installi Obtainium (F-Droid või GitHub)
2. Lisa rakendus:
   - URL: https://github.com/dz0nni/voicebell
   - APK Filter: debug\.apk$
3. Download → Install
```

**Detailne juhend:** [OBTAINIUM_SETUP.md](OBTAINIUM_SETUP.md)

---

## ✅ Kontroll

Veendu, et kõik töötab:

- [ ] GitHub repo eksisteerib
- [ ] Kood on GitHubis nähtav
- [ ] GitHub Actions käivitus edukalt
- [ ] Release lehel on APK fail
- [ ] APK installeeritav seadmes
- [ ] Obtainium leiab uuendused

---

## 📁 Olulised Failid

| Fail | Kirjeldus |
|------|-----------|
| `.github/workflows/release.yml` | ✅ Automaatne APK build tagide jaoks |
| `.github/workflows/build.yml` | ✅ Build iga push'i peale |
| `OBTAINIUM_SETUP.md` | 📱 Obtainiumiga kasutamine |
| `GITHUB_RELEASE_GUIDE.md` | 📖 Detailne release juhend |
| `TESTING_SUMMARY.md` | 🧪 Testimise ülevaade |
| `README.md` | 📝 Projekti ülevaade |
| `ARCHITECTURE.md` | 🏗️ Arhitektuur |

---

## 🔧 Troubleshooting

### ❌ Git push ebaõnnestub

```bash
# Kontrolli remote
git remote -v

# Paranda URL (asenda dz0nni!)
git remote set-url origin https://github.com/dz0nni/voicebell.git

# Proovi uuesti
git push -u origin main
```

### ❌ GitHub Actions ebaõnnestub

**Vaata logi:**
```
Actions tab → Klõpsa workflow'l → Vaata punast sammu
```

**Levinud parandused:**
```bash
# gradlew permission
chmod +x gradlew
git add gradlew
git commit -m "Fix: executable"
git push

# Loo tag uuesti
git tag -d v0.1.0
git push origin :refs/tags/v0.1.0
git tag v0.1.0
git push origin v0.1.0
```

### ❌ APK puudub Release'is

- Workflow läks läbi aga APK puudub?
- Kontrolli Actions logi "Create Release" sammu
- Veendu, et workflow ootas build'i valmimist

---

## 🎯 Järgmised Sammud

### Kui esimene release valmis:

**1. Jaga kasutajatega:**
```
📱 Download: https://github.com/dz0nni/voicebell/releases
📖 Obtainium setup: https://github.com/dz0nni/voicebell/blob/main/OBTAINIUM_SETUP.md
```

**2. Uuenduste tegemine:**
```bash
# Muuda koodi
git add .
git commit -m "Fix: description"
git push

# Uuenda versiooni (build.gradle.kts)
# versionName = "0.1.1"

# Loo uus release
git tag v0.1.1
git push origin v0.1.1
```

**3. Anna projekti teada:**
- Reddit: r/androidapps, r/opensource
- XDA Forums
- F-Droid (kui tahad ametlikku)

---

## 📊 Kokkuvõte

```
✅ Projekt valmis
✅ GitHub Actions configured
✅ Obtainium support
✅ Documentation complete

📤 git push origin v0.1.0
⏳ Wait ~10 minutes
✅ APK available on GitHub Releases
📱 Install with Obtainium
🎉 Done!
```

---

## 🆘 Abi

**Rohkem infot:**
- Üksikasjalik: [GITHUB_RELEASE_GUIDE.md](GITHUB_RELEASE_GUIDE.md)
- Obtainium: [OBTAINIUM_SETUP.md](OBTAINIUM_SETUP.md)
- Testimine: [TESTING_SUMMARY.md](TESTING_SUMMARY.md)

**Probleemid?**
- GitHub Issues: https://github.com/dz0nni/voicebell/issues

---

**Edu projektiga! 🎉**

*Need help? Asenda kõik `dz0nni` oma GitHub kasutajanimega!*
