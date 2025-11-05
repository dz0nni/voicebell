# Git Worktree Juhend VoiceBell Projektile

## Mis on Git Worktree?

Git worktree võimaldab sul omada **mitut töökopiat** samast repositooriumist **erinevates kaustades**. Igaüks töötab **erinevas harus**. See tähendab, et saad:
- Töötada **mitmes Claude sessioonis korraga**
- **Ei ole haru vahetamist** (git checkout) vaja
- **Zero conflicts** - iga worktree on isoleeritud

---

## 📁 Sinu Praegune Setup

### Loodud Worktree'd:

```
/docker/tannu/claude/clock            → main branch (põhikaust)
/docker/tannu/claude/clock-bugfixes   → bugfix/v0.1.5
/docker/tannu/claude/clock-features   → feature/alarm-improvements
/docker/tannu/claude/clock-testing    → testing/unit-tests
```

---

## 🚀 Kuidas Kasutada

### 1. **Ava Erinevad Claude Sessioonid**

#### Sessioon 1 - Bugfixid (SELLES SESSIOONIS):
```bash
cd /docker/tannu/claude/clock-bugfixes
```
- Paranda bugged
- Tee väikesed täiendused
- Quick patches

#### Sessioon 2 - Uued Featuredid:
```bash
cd /docker/tannu/claude/clock-features
```
- Lisa uued funktsioonid
- Suuremad muudatused
- Alarm tone picker, custom sounds

#### Sessioon 3 - Testimine:
```bash
cd /docker/tannu/claude/clock-testing
```
- Kirjuta unit teste
- Integration tests
- CI/CD improvements

#### Sessioon 4 - Main (vajadusel):
```bash
cd /docker/tannu/claude/clock
```
- Dokumentatsiooni uuendused
- README muudatused
- Quick checks

---

## 📝 Töövoog (Workflow)

### A. Bugfixi Workflow (clock-bugfixes)

```bash
# 1. Mine bugfix kausta
cd /docker/tannu/claude/clock-bugfixes

# 2. Kontrolli, kus sa oled
git branch  # Peaks näitama: * bugfix/v0.1.5
pwd         # Peaks näitama: /docker/tannu/claude/clock-bugfixes

# 3. Tee muudatused (Claude aitab)
# ... paranda bugid ...

# 4. Commit
git add .
git commit -m "fix: resolve alarm crash on Android 14"

# 5. Push (esimene kord)
git push -u origin bugfix/v0.1.5

# 6. Push (järgnevad korrad)
git push
```

### B. Feature Workflow (clock-features)

```bash
# 1. Mine feature kausta
cd /docker/tannu/claude/clock-features

# 2. Kontrolli
git branch  # Peaks näitama: * feature/alarm-improvements

# 3. Tee muudatused
# ... lisa uued featuredid ...

# 4. Commit
git add .
git commit -m "feat: add alarm tone picker"

# 5. Push
git push -u origin feature/alarm-improvements
```

### C. Merge'imine Main'i

#### Variant 1: GitHub Pull Request (SOOVITATAV)
```bash
# 1. Push oma branch
cd /docker/tannu/claude/clock-bugfixes
git push -u origin bugfix/v0.1.5

# 2. Loo PR GitHubis
gh pr create --title "Fix: Alarm crashes on Android 14" \
  --body "Fixed crashes reported in #123"

# 3. Merge PR kui valmis
gh pr merge --squash
```

#### Variant 2: Otse Main'i (Kui kindel)
```bash
# 1. Mine main kausta
cd /docker/tannu/claude/clock

# 2. Pull uusim kood
git pull origin main

# 3. Merge bugfix branch
git merge bugfix/v0.1.5

# 4. Push
git push origin main

# 5. Cleanup branch (kui valmis)
git branch -d bugfix/v0.1.5
git push origin --delete bugfix/v0.1.5
```

---

## 🔄 Sünkroniseerimine

### Main muutub? Update oma worktree'd:

```bash
# Main kaustas
cd /docker/tannu/claude/clock
git pull origin main

# Bugfix kaustas - võta main muudatused
cd /docker/tannu/claude/clock-bugfixes
git merge main
# VÕI
git rebase main

# Feature kaustas - sama
cd /docker/tannu/claude/clock-features
git merge main
```

---

## 📋 Kasulikud Käsud

### Vaata kõiki worktree'sid:
```bash
git worktree list
```

### Vaata praegust haru:
```bash
git branch
```

### Vaata kõiki harusid (local + remote):
```bash
git branch -a
```

### Switch worktree'de vahel (ei ole vaja!):
```bash
# Lihtsalt cd teise kausta
cd /docker/tannu/claude/clock-features
```

### Remove worktree (kui enam ei vaja):
```bash
# 1. Eemalda worktree
git worktree remove /docker/tannu/claude/clock-bugfixes

# 2. Kustuta branch (kui merged)
git branch -d bugfix/v0.1.5

# 3. Kustuta remote branch
git push origin --delete bugfix/v0.1.5
```

### Lisa uus worktree:
```bash
cd /docker/tannu/claude/clock
git worktree add ../clock-voice -b feature/voice-commands
```

---

## ⚠️ TÄHTIS: Väldi vigu!

### ✅ ÕiGE:
```bash
# Igal worktree'l oma branch
cd /docker/tannu/claude/clock-bugfixes  # → bugfix/v0.1.5
cd /docker/tannu/claude/clock-features  # → feature/alarm-improvements
```

### ❌ VALE:
```bash
# ÄRA kasuta sama branchi mitmes worktree's
git worktree add ../clock-duplicate -b bugfix/v0.1.5  # VIGA! Branch juba kasutusel
```

### ✅ ÕIGE commit message:
```bash
git commit -m "fix: resolve alarm crash on startup"
git commit -m "feat: add alarm tone picker UI"
git commit -m "test: add unit tests for AlarmViewModel"
```

### ❌ VALE commit message:
```bash
git commit -m "update"  # Liiga ebamäärane
git commit -m "asdasd"  # Ei kirjelda muudatust
```

---

## 🎯 Näide Töövoog: Täna

### Kell 10:00 - Bugfixid (Sessioon 1)
```bash
cd /docker/tannu/claude/clock-bugfixes
# Claude aitab parandada 3 bug'i
git add .
git commit -m "fix: resolve three critical bugs"
git push -u origin bugfix/v0.1.5
```

### Kell 11:00 - Features (Sessioon 2 - PARALLEELSELT!)
```bash
cd /docker/tannu/claude/clock-features
# Claude aitab lisada alarm tone picker
git add .
git commit -m "feat: implement alarm tone picker"
git push -u origin feature/alarm-improvements
```

### Kell 12:00 - Merge (Sessioon 1 või käsitsi)
```bash
# Loo PRid GitHubis
cd /docker/tannu/claude/clock-bugfixes
gh pr create --title "Fix: Critical bugs v0.1.5"

cd /docker/tannu/claude/clock-features
gh pr create --title "Feature: Alarm tone picker"

# Merge PRid kui valmis
# Review ja merge GitHubis
```

### Kell 13:00 - Release
```bash
cd /docker/tannu/claude/clock
git pull origin main  # Pull kõik merged changes
# ... update version number ...
# ... create release ...
```

---

## 🤔 Millal Kasutada Millist Worktree'd?

| Worktree | Kasutus | Claude Sessioon |
|----------|---------|-----------------|
| `clock` (main) | Dokumentatsioon, README, quick checks | Sessioon 4 |
| `clock-bugfixes` | Bugfixid, väikesed parandused, quick patches | Sessioon 1 ✅ |
| `clock-features` | Uued featuredid, suuremad muudatused | Sessioon 2 ✅ |
| `clock-testing` | Unit testid, CI/CD, testide kirjutamine | Sessioon 3 |

---

## 🆘 Probleemide Lahendamine

### Probleem: "Branch already checked out"
```bash
# Lahendus: Kasuta erinevat branchi või remove vana worktree
git worktree list  # Vaata, kus branch on
git worktree remove /path/to/worktree
```

### Probleem: "Merge conflict"
```bash
# Lahendus: Resolve konfliktid
git status  # Vaata, millised failid on conflict
# ... paranda konfliktid käsitsi ...
git add .
git commit -m "resolve merge conflicts"
```

### Probleem: "Worktree on vale branch'il"
```bash
# Lahendus: Checkout õige branch
cd /docker/tannu/claude/clock-bugfixes
git checkout bugfix/v0.1.5
```

### Probleem: "Ei tea, kus ma olen"
```bash
# Lahendus: Kontrolli
pwd          # Vaata kausta
git branch   # Vaata branchi
git status   # Vaata state'i
```

---

## 📚 Lisainfo

### Git Worktree dokumentatsioon:
```bash
man git-worktree
git worktree --help
```

### Online:
- https://git-scm.com/docs/git-worktree
- https://www.gitkraken.com/learn/git/git-worktree

---

## ✅ Kontroll-nimekiri (Checklist)

Enne tööalustamist:
- [ ] Kontrolli, millises kaustas sa oled: `pwd`
- [ ] Kontrolli, millises branch'is sa oled: `git branch`
- [ ] Veendu, et see on õige worktree: `git worktree list`

Enne commiti:
- [ ] Kontrolli muudatusi: `git status`
- [ ] Vaata diffe: `git diff`
- [ ] Kirjuta hea commit message: `git commit -m "type: description"`

Enne push'i:
- [ ] Kontrolli, et oled õiges branch'is: `git branch`
- [ ] Testi, et kood töötab: `./gradlew test`
- [ ] Push: `git push`

---

**Edu paralleelse arendamisega! 🚀**

*Küsimused? Küsi Claude'ilt!*
