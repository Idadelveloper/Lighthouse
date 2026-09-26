#!/usr/bin/env bash

set -euo pipefail

repo_root="$(git rev-parse --show-toplevel)"
cd "$repo_root"

blocked_patterns=(
  'BuildConfig\.[A-Z0-9_]*API_KEY'
  'generativelanguage\.googleapis\.com/[^[:space:]"'\'']*[?&]key='
  'GEMINI_API_KEY[[:space:]]*='
)

failed=0
if ! rg -Fq 'ignoreList.add("GEMINI_API_KEY")' app/build.gradle.kts; then
  echo "FAIL: old local Gemini configuration can still be packaged by the Secrets Gradle Plugin." >&2
  exit 1
fi

for pattern in "${blocked_patterns[@]}"; do
  if rg --pcre2 -l "$pattern" app/src/main README.md .env.example >/dev/null 2>&1; then
    failed=1
  fi
done

if [[ $failed -ne 0 ]]; then
  echo "FAIL: Android source or setup docs still permit a durable Gemini API key in the client." >&2
  echo "Use Firebase AI Logic with App Check, or a backend that mints short-lived credentials." >&2
  exit 1
fi

echo "PASS: no durable Gemini API key path is present in Android source or setup docs"
