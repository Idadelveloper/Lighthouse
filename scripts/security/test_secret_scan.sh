#!/usr/bin/env bash

set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
scanner="$script_dir/secret_scan.sh"

if [[ ! -x "$scanner" ]]; then
  echo "FAIL: secret scanner is missing or not executable" >&2
  exit 1
fi

temp_root="$(mktemp -d)"
trap 'rm -rf "$temp_root"' EXIT

git -C "$temp_root" init -q
git -C "$temp_root" config user.email "security-test@example.invalid"
git -C "$temp_root" config user.name "Security Test"

cat > "$temp_root/.gitignore" <<'EOF'
.env
local.properties
google-services.json
EOF

cat > "$temp_root/placeholders.env.example" <<'EOF'
GEMINI_API_KEY=MY_GEMINI_API_KEY
MAPS_API_KEY=YOUR_MAPS_API_KEY
EOF

cp "$scanner" "$temp_root/secret_scan.sh"
chmod +x "$temp_root/secret_scan.sh"

if ! (cd "$temp_root" && ./secret_scan.sh --workspace >/dev/null); then
  echo "FAIL: documented placeholders should be allowed" >&2
  exit 1
fi

google_secret="AI""za""Sy""0123456789abcdefghijklmnopqrstuvwxyz"
printf 'MAPS_API_KEY=%s\n' "$google_secret" > "$temp_root/leak.properties"

set +e
google_output="$(cd "$temp_root" && ./secret_scan.sh --workspace 2>&1)"
google_status=$?
set -e

if [[ $google_status -eq 0 ]]; then
  echo "FAIL: Google-style credential was not blocked" >&2
  exit 1
fi
if [[ "$google_output" == *"$google_secret"* ]]; then
  echo "FAIL: scanner printed a detected credential" >&2
  exit 1
fi
if [[ "$google_output" != *"leak.properties"* ]]; then
  echo "FAIL: scanner did not identify the affected file" >&2
  exit 1
fi

rm "$temp_root/leak.properties"
private_marker="-----BEGIN ""PRIVATE KEY""-----"
printf '%s\nplaceholder\n' "$private_marker" > "$temp_root/private.pem"

set +e
private_output="$(cd "$temp_root" && ./secret_scan.sh --workspace 2>&1)"
private_status=$?
set -e

if [[ $private_status -eq 0 ]]; then
  echo "FAIL: private-key marker was not blocked" >&2
  exit 1
fi
if [[ "$private_output" == *"$private_marker"* ]]; then
  echo "FAIL: scanner printed private-key material" >&2
  exit 1
fi

rm "$temp_root/private.pem"
git -C "$temp_root" add .
git -C "$temp_root" commit -qm "clean baseline"

if ! (cd "$temp_root" && ./secret_scan.sh --all >/dev/null); then
  echo "FAIL: clean workspace, staged state, and history should pass" >&2
  exit 1
fi

echo "PASS: placeholders allowed, credentials blocked, findings redacted, clean repository accepted"
