# Voice Commands Reference

VoiceBell supports natural English voice commands for hands-free control of alarms and timers. All voice recognition happens **100% offline** using the Vosk speech recognition engine.

## Table of Contents

- [Getting Started](#getting-started)
- [Alarm Commands](#alarm-commands)
- [Timer Commands](#timer-commands)
- [Query Commands](#query-commands)
- [Cancel Commands](#cancel-commands)
- [Tips for Best Results](#tips-for-best-results)
- [Troubleshooting](#troubleshooting)

## Getting Started

### First Time Setup

1. Grant microphone permission when prompted
2. Tap the microphone button on any screen
3. Speak your command clearly
4. Wait for visual feedback

### How to Use

1. **Tap** the microphone button (🎤)
2. **Wait** for the "Listening..." indicator
3. **Speak** your command naturally
4. **Confirm** the recognized command

The app will show:
- **Listening...** - Microphone is active
- **Processing...** - Analyzing your command
- **Success** - Command executed
- **Error** - Could not understand, try again

## Alarm Commands

### Set Alarm - Basic

Set a single alarm for a specific time.

| Command | Result |
|---------|--------|
| "Set alarm for 7 AM" | Alarm at 7:00 AM |
| "Set alarm for 7:30 AM" | Alarm at 7:30 AM |
| "Set alarm for 10 PM" | Alarm at 10:00 PM |
| "Wake me at 6 AM" | Alarm at 6:00 AM |
| "Wake me up at 6:30 AM" | Alarm at 6:30 AM |

**Supported formats:**
- "Set alarm for [TIME]"
- "Wake me at [TIME]"
- "Wake me up at [TIME]"
- "Alarm for [TIME]"

**Time formats:**
- 12-hour with AM/PM: "7 AM", "7:30 PM"
- Without minutes: "7 AM" (assumes :00)
- With minutes: "7:30 AM", "6:45 PM"

### Set Alarm - Repeating

Set alarms that repeat on specific days.

| Command | Result |
|---------|--------|
| "Set alarm for 7 AM on weekdays" | Mon-Fri at 7:00 AM |
| "Set alarm for 8 AM on weekends" | Sat-Sun at 8:00 AM |
| "Set alarm for 9 AM on Monday" | Every Monday at 9:00 AM |
| "Set alarm for 6:30 AM Monday through Friday" | Mon-Fri at 6:30 AM |
| "Wake me at 7 AM every day" | Daily at 7:00 AM |

**Supported day patterns:**
- **Weekdays**: "weekdays", "Monday through Friday", "Monday to Friday"
- **Weekends**: "weekends", "Saturday and Sunday"
- **Specific days**: "Monday", "Tuesday", "Monday and Wednesday"
- **Every day**: "every day", "daily"

### Set Alarm - With Label

Give your alarm a custom label.

| Command | Result |
|---------|--------|
| "Set alarm for 7 AM called Work" | Alarm labeled "Work" |
| "Set alarm for 8 PM labeled Medication" | Alarm labeled "Medication" |
| "Wake me at 6 AM for Gym" | Alarm labeled "Gym" |

**Supported formats:**
- "... called [LABEL]"
- "... labeled [LABEL]"
- "... for [LABEL]"

## Timer Commands

### Set Timer - Duration

Start a countdown timer.

| Command | Result |
|---------|--------|
| "Set timer for 5 minutes" | 5-minute timer |
| "Set timer for 30 seconds" | 30-second timer |
| "Set timer for 1 hour" | 1-hour timer |
| "Set timer for 1 hour 30 minutes" | 1.5-hour timer |
| "Timer for 10 minutes" | 10-minute timer |
| "Start 5 minute timer" | 5-minute timer |

**Supported formats:**
- "Set timer for [DURATION]"
- "Timer for [DURATION]"
- "Start [DURATION] timer"

**Duration formats:**
- Minutes only: "5 minutes", "30 minutes"
- Seconds only: "30 seconds", "90 seconds"
- Hours only: "1 hour", "2 hours"
- Combined: "1 hour 30 minutes", "2 hours 15 minutes"

### Set Timer - With Label

Create a labeled timer.

| Command | Result |
|---------|--------|
| "Set timer for 10 minutes called Pizza" | Timer labeled "Pizza" |
| "Timer for 5 minutes for Tea" | Timer labeled "Tea" |
| "Start 20 minute timer labeled Workout" | Timer labeled "Workout" |

## Query Commands

### Check Alarms

Ask about your configured alarms.

| Command | Result |
|---------|--------|
| "What alarms are set?" | Lists all active alarms |
| "Show my alarms" | Displays alarm list |
| "Do I have any alarms?" | Shows alarm status |
| "When is my next alarm?" | Shows next scheduled alarm |

### Check Timers

Ask about running timers.

| Command | Result |
|---------|--------|
| "What timers are running?" | Lists active timers |
| "Show my timers" | Displays timer list |
| "How much time is left?" | Shows remaining time on current timer |

## Cancel Commands

### Cancel Alarm

Delete or disable alarms.

| Command | Result |
|---------|--------|
| "Cancel alarm" | Shows list to select which alarm |
| "Delete alarm" | Shows list to select which alarm |
| "Remove alarm for 7 AM" | Deletes alarm at 7:00 AM |
| "Cancel my 6:30 AM alarm" | Deletes alarm at 6:30 AM |
| "Turn off alarm" | Shows list to select which alarm |

**Note**: If you have multiple alarms, the app will show a list to choose from.

### Cancel Timer

Stop running timers.

| Command | Result |
|---------|--------|
| "Cancel timer" | Stops current timer |
| "Stop timer" | Stops current timer |
| "Delete timer" | Removes timer |

## Advanced Features

### Natural Language Variations

VoiceBell understands various phrasings:

**"Set alarm"** variations:
- "Set an alarm for..."
- "Create alarm for..."
- "Make alarm for..."
- "Add alarm for..."

**"Wake me"** variations:
- "Wake me up..."
- "Wake me at..."
- "Wake up at..."

**Time variations:**
- "7 o'clock" → 7:00
- "Half past 7" → 7:30
- "Quarter to 8" → 7:45
- "Noon" → 12:00 PM
- "Midnight" → 12:00 AM

### Combining Commands

You can combine multiple parameters:

| Command | Result |
|---------|--------|
| "Set alarm for 7 AM on weekdays called Work" | Repeating labeled alarm |
| "Wake me at 6:30 AM every Monday and Wednesday for Gym" | Custom repeat alarm |
| "Set timer for 10 minutes called Pasta" | Labeled timer |

## Tips for Best Results

### Environment

- **Quiet room**: Voice recognition works best in quiet environments
- **Reduce background noise**: Turn down TV/music
- **Close to device**: Speak within 1-2 feet of microphone

### Speaking

- **Clear pronunciation**: Enunciate each word
- **Normal pace**: Don't speak too fast or too slow
- **Natural tone**: Use your normal speaking voice
- **Consistent volume**: Speak at moderate volume

### Commands

- **Use exact phrases**: Stick to documented command formats
- **Be specific**: "7 AM" is clearer than "morning"
- **Pause briefly**: Wait for "Listening..." before speaking
- **One command at a time**: Don't chain multiple commands

### Common Patterns

**Good examples:**
- ✅ "Set alarm for 7 AM"
- ✅ "Set timer for 5 minutes"
- ✅ "What alarms are set?"

**Avoid:**
- ❌ "Um... set alarm for, like, 7 AM" (filler words)
- ❌ "Setalarmfor7AM" (too fast, no pauses)
- ❌ "Set alarm for sometime in the morning" (vague)

## Troubleshooting

### "Could not understand" Error

**Possible causes:**
1. Background noise too loud
2. Speaking too fast/slow
3. Microphone obstructed
4. Command not in recognized format

**Solutions:**
- Move to quieter location
- Speak more clearly
- Check microphone isn't blocked
- Use exact command phrases from this guide

### Wrong Time Recognized

**Problem**: Said "7 AM" but got "11 AM"

**Solutions:**
- Emphasize AM/PM: "7 **A** **M**"
- Spell it out: "7 in the morning"
- Use 24-hour clarification: "7 o'clock in the morning"

### Microphone Permission Denied

**Problem**: Can't access microphone

**Solutions:**
1. Go to Android Settings
2. Apps → VoiceBell → Permissions
3. Enable Microphone permission
4. Restart VoiceBell

### Voice Recognition Slow

**Problem**: Takes too long to process

**Possible causes:**
1. First-time model loading (normal)
2. Low device resources
3. Long/complex command

**Solutions:**
- Wait for model to load (first use only)
- Close other apps
- Use shorter, simpler commands

### Accent Not Recognized

**Problem**: Non-native English accent causing errors

**Current limitation**: Vosk model is optimized for standard American English

**Workarounds:**
- Speak with clearer enunciation
- Use standard American pronunciation
- Fall back to manual UI input

**Future**: We plan to add more language models in future versions

## Supported vs. Future Commands

### Currently Supported ✅

- ✅ Set alarm with time
- ✅ Set repeating alarms
- ✅ Set timer with duration
- ✅ Query alarms and timers
- ✅ Cancel alarms and timers
- ✅ Labeled alarms and timers

### Coming Soon 🔜

- 🔜 "Snooze for 10 minutes"
- 🔜 "Remind me to [task]"
- 🔜 "Set alarm in 8 hours"
- 🔜 "Postpone alarm"
- 🔜 Multi-language support

## Privacy Note

All voice recognition happens **100% offline**:

- Voice data **never leaves your device**
- No internet connection required
- No data sent to servers
- Complete privacy guaranteed

The Vosk speech recognition model runs entirely on your Android device using the local CPU.

## Feedback

Having trouble with a specific command? Help us improve!

- Report issues: [GitHub Issues](https://github.com/dz0nni/voicebell/issues)
- Suggest new commands: [GitHub Discussions](https://github.com/dz0nni/voicebell/discussions)

---

**Remember**: Voice commands are a convenience feature. All functionality is also available through the touch interface.
