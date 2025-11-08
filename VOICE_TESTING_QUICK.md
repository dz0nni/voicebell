# Voice Recognition Quick Testing Guide

**ENNE TESTIMIST:** Sync testing worktree dev worktree'ga!

## ⚡ 1-Minute Sync & Build

```bash
# 1. Sync with dev worktree
cd /Users/kratt/claude/clock-testing
git fetch --all
git checkout feature/alarm-improvements
git pull origin feature/alarm-improvements

# 2. Verify sync (commit hash should match dev)
git log -1 --oneline
# Expected: d674e2e fix: use partial result when final result is empty

# 3. Quick build
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export ANDROID_HOME=~/Library/Android/sdk
./gradlew assembleDebug

# 4. Install on phone
adb -s 38091FDJH00H1Z install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 🧪 Quick Voice Test (2 minutes)

### Test 1: Timer Command

```bash
# Clear logs and start monitoring
adb -s 38091FDJH00H1Z logcat -c && \
echo "📱 Ready to test voice command" && \
echo "1. Open VoiceBell app" && \
echo "2. Go to Voice Command screen" && \
echo "3. Tap microphone" && \
echo "4. Say: 'set timer 5 minutes'" && \
echo "5. Tap again to stop" && \
echo "" && \
adb -s 38091FDJH00H1Z logcat | grep -E "(VoskWrapper|ExecuteVoice|VoiceCommand|Timer|Alarm|partial text)"
```

**Expected logs:**
```
✅ Saved partial text: timer for five minutes
✅ Final result empty, using last partial text: timer for five minutes
✅ Recognized text: timer for five minutes
✅ Executing voice command: timer for five minutes
✅ Command executed successfully: Timer set for 5 minutes
```

### Test 2: Alarm Command

```bash
# Say: "set alarm 7 AM"
# Expected: Alarm created for 07:00
```

---

## 📊 Log Analysis Commands

### Check if partial fallback works:

```bash
adb -s 38091FDJH00H1Z logcat -d | grep -A2 "Saved partial text"
# Should see partial text being saved
```

### Check command execution:

```bash
adb -s 38091FDJH00H1Z logcat -d | grep "Command executed"
# Should see "Command executed successfully"
```

### Check for errors:

```bash
adb -s 38091FDJH00H1Z logcat -d | grep -E "(FATAL|Exception|Error)" | tail -20
```

---

## ✅ Success Criteria

**MUST PASS:**
- [ ] Partial text is saved during recognition
- [ ] Final result uses partial text when empty
- [ ] ExecuteVoiceCommandUseCase is called
- [ ] Timer/Alarm is created in database
- [ ] Success message broadcast to UI

**VERIFY:**
- [ ] Timer appears in Timers tab
- [ ] Alarm appears in Alarms tab
- [ ] No crashes or exceptions

---

## 🐛 Common Issues

### Issue 1: "No text in result"

**Old behavior (BEFORE fix):**
```
Partial result: {"partial": "timer for five"}
Final result: {"text": ""}  ← EMPTY!
No text in result  ❌
```

**New behavior (AFTER fix):**
```
Saved partial text: timer for five
Final result empty, using last partial text: timer for five
Recognized text: timer for five  ✅
Command executed successfully  ✅
```

### Issue 2: Command not executing

**Check:**
```bash
adb -s 38091FDJH00H1Z logcat -d | grep "ExecuteVoice"
```

Should see:
```
ExecuteVoiceCommand: Executing voice command: ...
ExecuteVoiceCommand: Command executed successfully: ...
```

### Issue 3: Vosk model not loaded

**Check:**
```bash
adb -s 38091FDJH00H1Z logcat -d | grep "Vosk model"
```

Should see:
```
Vosk model initialized successfully
```

---

## 🚀 Full Test Script (Copy-Paste)

```bash
#!/bin/bash
# Voice Recognition Full Test

PHONE="38091FDJH00H1Z"

echo "🔄 Syncing testing worktree..."
cd /Users/kratt/claude/clock-testing
git fetch --all && git checkout feature/alarm-improvements && git pull

echo "🏗️  Building APK..."
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export ANDROID_HOME=~/Library/Android/sdk
./gradlew assembleDebug

echo "📱 Installing on phone..."
adb -s $PHONE install -r app/build/outputs/apk/debug/app-debug.apk

echo "📋 Clearing logs..."
adb -s $PHONE logcat -c

echo ""
echo "✅ Setup complete!"
echo ""
echo "📱 MANUAL STEPS:"
echo "1. Open VoiceBell app"
echo "2. Tap microphone button"
echo "3. Say: 'set timer 5 minutes'"
echo "4. Tap microphone again to stop"
echo ""
echo "📊 Monitoring logs... (Ctrl+C to stop)"
echo ""

adb -s $PHONE logcat | grep --line-buffered -E "(Saved partial|using last partial|Recognized text|Command executed|Timer.*created|Alarm.*created)"
```

Save as `test_voice.sh` and run: `chmod +x test_voice.sh && ./test_voice.sh`

---

## 📝 Report Template

After testing, report results:

```markdown
## Voice Recognition Test Results

**Date:** 2025-11-08
**Commit:** d674e2e fix: use partial result when final result is empty
**Device:** 38091FDJH00H1Z

### Test 1: Timer "5 minutes"
- [ ] PASS / [ ] FAIL
- Partial text saved: YES / NO
- Fallback used: YES / NO
- Command executed: YES / NO
- Timer created: YES / NO
- Logs: [attach relevant logs]

### Test 2: Alarm "7 AM"
- [ ] PASS / [ ] FAIL
- [same checklist]

### Issues Found:
1. [Issue description]
2. [Issue description]

### Conclusion:
✅ All tests passed - ready to merge
❌ Bugs found - needs fixes
```

---

**Next Steps After Testing:**
1. If tests pass → Report to dev session → Merge to main
2. If tests fail → Report bugs to bugs session → Fix → Re-test
