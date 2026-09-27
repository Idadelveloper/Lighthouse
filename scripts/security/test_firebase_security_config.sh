#!/usr/bin/env bash

set -euo pipefail

repo_root="$(git rev-parse --show-toplevel)"
cd "$repo_root"

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

rg -q 'android:name="\.LighthouseApplication"' app/src/main/AndroidManifest.xml \
  || fail "LighthouseApplication is not registered in the manifest"
rg -q 'PlayIntegrityAppCheckProviderFactory' app/src/main/java/com/example/LighthouseApplication.kt \
  || fail "release App Check does not use Play Integrity"
rg -q 'DebugAppCheckProviderFactory' app/src/main/java/com/example/LighthouseApplication.kt \
  || fail "debug App Check provider is missing"
rg -q 'implementation\(libs\.firebase\.appcheck\.playintegrity\)' app/build.gradle.kts \
  || fail "Play Integrity dependency is missing from production"
rg -q 'debugImplementation\(libs\.firebase\.appcheck\.debug\)' app/build.gradle.kts \
  || fail "debug provider is not scoped to debug builds"

if rg -q 'implementation\(libs\.firebase\.appcheck\.debug\)' app/build.gradle.kts; then
  fail "debug App Check provider would ship in release builds"
fi
if rg -q 'firebase-appcheck-recaptcha' gradle/libs.versions.toml app/build.gradle.kts; then
  fail "web reCAPTCHA provider remains in the Android build"
fi

echo "PASS: source config selects debug-only App Check and release Play Integrity"
