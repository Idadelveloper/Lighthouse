#!/usr/bin/env bash

set -euo pipefail

repo_root="$(git rev-parse --show-toplevel)"
cd "$repo_root"

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

manifest="app/src/main/AndroidManifest.xml"
engine="app/src/main/java/com/example/service/GeminiLiveAudioEngine.kt"
modal="app/src/main/java/com/example/ui/components/GeminiLiveVoiceModal.kt"

rg -q 'android:allowBackup="false"' "$manifest" \
  || fail "local contacts and saved walks are still eligible for Android backup"

if rg -q 'android\.permission\.(RECORD_AUDIO|MODIFY_AUDIO_SETTINGS)' "$manifest"; then
  fail "public build still requests audio permissions"
fi

if rg -q 'Firebase\.ai|\.liveModel\(|\.connect\(\)|\.startAudioConversation\(|SpeechRecognizer|TextToSpeech\(' "$engine"; then
  fail "public voice boundary still contains an active audio or Live implementation"
fi

if rg -q 'ActivityResultContracts\.RequestPermission' "$modal"; then
  fail "public voice UI still requests microphone permission"
fi

rg -q 'does not request microphone access or send audio or transcripts' "$modal" \
  || fail "public voice UI does not disclose its disabled behavior"

echo "PASS: public build disables voice capture and Android backup"
