#!/usr/bin/env python3
"""
Generate test audio files for VoiceBell voice command testing.

This script creates 5 different voice command audio files in the correct
format for Vosk (16kHz, mono, WAV).

Requirements:
    pip install gtts
    brew install ffmpeg (for audio conversion)

Usage:
    python3 generate_test_audio.py
"""

from gtts import gTTS
import os
import subprocess

# Voice commands to generate
commands = [
    {
        "text": "set timer for 2 minutes",
        "filename": "test_set_timer_2min.wav",
        "description": "Sets a 2-minute timer"
    },
    {
        "text": "set timer for 30 seconds",
        "filename": "test_set_timer_30sec.wav",
        "description": "Sets a 30-second timer"
    },
    {
        "text": "set alarm for 7 AM",
        "filename": "test_set_alarm_7am.wav",
        "description": "Sets alarm for 7:00 AM"
    },
    {
        "text": "wake me up at 6 30",
        "filename": "test_wake_up_630.wav",
        "description": "Sets alarm for 6:30"
    },
    {
        "text": "set timer for 5 minutes",
        "filename": "test_set_timer_5min.wav",
        "description": "Sets a 5-minute timer"
    }
]

def generate_audio_file(text, output_filename):
    """
    Generate audio file from text using Google TTS.
    Converts to Vosk-compatible format (16kHz, mono, WAV).
    """
    print(f"Generating: {output_filename}")
    print(f"  Text: \"{text}\"")

    # Generate TTS audio (creates MP3)
    tts = gTTS(text=text, lang='en', slow=False)
    temp_mp3 = output_filename.replace('.wav', '_temp.mp3')
    tts.save(temp_mp3)

    # Convert to WAV, 16kHz, mono using ffmpeg (Vosk requirements)
    try:
        subprocess.run([
            'ffmpeg', '-i', temp_mp3,
            '-ar', '16000',  # 16kHz sample rate
            '-ac', '1',      # Mono
            '-y',            # Overwrite output file
            output_filename
        ], check=True, capture_output=True, text=True)
    except subprocess.CalledProcessError as e:
        print(f"  ✗ Error converting audio: {e.stderr}")
        raise

    # Clean up temp file
    os.remove(temp_mp3)

    # Get file size and duration using ffprobe
    size_kb = os.path.getsize(output_filename) / 1024
    try:
        result = subprocess.run([
            'ffprobe', '-v', 'error',
            '-show_entries', 'format=duration',
            '-of', 'default=noprint_wrappers=1:nokey=1',
            output_filename
        ], capture_output=True, text=True, check=True)
        duration = float(result.stdout.strip())
        print(f"  ✓ Created: {size_kb:.1f} KB, {duration:.1f}s")
    except:
        print(f"  ✓ Created: {size_kb:.1f} KB")
    print()

def main():
    """Generate all test audio files."""
    print("=" * 60)
    print("VoiceBell Test Audio Generator")
    print("=" * 60)
    print()

    output_dir = "test_audio"
    os.makedirs(output_dir, exist_ok=True)

    print(f"Output directory: {output_dir}/")
    print()

    for i, cmd in enumerate(commands, 1):
        output_path = os.path.join(output_dir, cmd["filename"])
        print(f"[{i}/{len(commands)}] {cmd['description']}")
        generate_audio_file(cmd["text"], output_path)

    print("=" * 60)
    print("✓ All test audio files generated successfully!")
    print()
    print("Files created:")
    for cmd in commands:
        filepath = os.path.join(output_dir, cmd["filename"])
        size_kb = os.path.getsize(filepath) / 1024
        print(f"  - {cmd['filename']} ({size_kb:.1f} KB)")
        print(f"    → {cmd['description']}")

    print()
    print("=" * 60)
    print("How to use these files:")
    print()
    print("1. Transfer to emulator:")
    print("   adb push test_audio/*.wav /sdcard/Download/")
    print()
    print("2. Play through virtual microphone:")
    print("   adb shell am start -a android.intent.action.VIEW \\")
    print("     -d file:///sdcard/Download/test_set_timer_2min.wav")
    print()
    print("3. Or use: ffplay, VLC, or any audio player that routes")
    print("   to the emulator's virtual microphone input")
    print("=" * 60)

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\nAborted by user")
    except ImportError as e:
        print(f"\n❌ Missing dependency: {e}")
        print("\nPlease install required packages:")
        print("  pip3 install gtts")
        print("\nYou also need ffmpeg:")
        print("  brew install ffmpeg")
    except Exception as e:
        print(f"\n❌ Error: {e}")
        import traceback
        traceback.print_exc()
