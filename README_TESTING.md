# Testing Worktree - Quick Start

## 📍 Location
`/Users/kratt/claude/clock-testing`

## 🎯 Purpose
Fast, isolated testing of voice recognition and other features before merging to main.

## ⚡ Quick Test (1 command)

```bash
cd /Users/kratt/claude/clock-testing && ./test_voice.sh
```

This will:
1. Sync with dev worktree (feature/alarm-improvements branch)
2. Build APK
3. Install on phone (38091FDJH00H1Z)
4. Monitor logs

Then follow manual steps to test voice commands.

## 📚 Documentation

- **[VOICE_TESTING_QUICK.md](VOICE_TESTING_QUICK.md)** - Comprehensive voice testing guide
- **[test_voice.sh](test_voice.sh)** - Automated test script

## 🔄 Always Sync First!

**CRITICAL:** Before testing, always sync with dev worktree:

```bash
cd /Users/kratt/claude/clock-testing
git fetch --all
git checkout feature/alarm-improvements
git pull origin feature/alarm-improvements
```

Why? Testing worktree may be 7+ commits behind dev!

## 🐛 Current Bug Being Fixed

**Issue:** Voice recognition returns empty final result

**Fix:** Use last partial result as fallback

**Expected behavior after fix:**
```
Partial: "timer for five minutes"  ✅ saved
Final: ""  ❌ empty
→ Uses partial: "timer for five minutes"  ✅
→ Command executed: Timer created  ✅
```

## 📱 Test Device

- **ID:** 38091FDJH00H1Z
- **Permissions:** RECORD_AUDIO (already granted)
- **Connection:** USB debugging enabled

## 🚀 Full Workflow

1. **Dev session** makes code changes → commits
2. **You** switch to testing session
3. **Testing session** runs `./test_voice.sh`
4. **You** test manually on phone
5. **Testing session** reports results to dev session
6. IF bugs found → **Bugs session** fixes → back to step 1
7. IF tests pass → **Dev session** merges to main

## 📊 Success Criteria

- [ ] Partial text saved during recognition
- [ ] Final result uses fallback when empty
- [ ] Voice commands execute (create timer/alarm)
- [ ] No crashes
- [ ] Logs show "Command executed successfully"

## 🆘 Troubleshooting

### "No such file or directory: ./test_voice.sh"
```bash
cd /Users/kratt/claude/clock-testing
chmod +x test_voice.sh
```

### Phone not found
```bash
adb devices
# Should show: 38091FDJH00H1Z
```

### Build fails
```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export ANDROID_HOME=~/Library/Android/sdk
./gradlew clean assembleDebug
```

### Sync fails (merge conflicts)
```bash
# Hard reset to dev branch
git fetch --all
git reset --hard origin/feature/alarm-improvements
```

---

**Need more details?** Read [VOICE_TESTING_QUICK.md](VOICE_TESTING_QUICK.md)
