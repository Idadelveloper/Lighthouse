#!/usr/bin/env bash

set -euo pipefail

mode="${1:---workspace}"
case "$mode" in
  --workspace|--staged|--history|--all) ;;
  *)
    echo "Usage: $0 [--workspace|--staged|--history|--all]" >&2
    exit 64
    ;;
esac

if ! command -v rg >/dev/null 2>&1; then
  echo "ERROR: ripgrep (rg) is required for redacted secret scanning" >&2
  exit 69
fi

repo_root="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
cd "$repo_root"

patterns=(
  'AIza[0-9A-Za-z_-]{30,}'
  '-----BEGIN[[:space:]]+(?:RSA[[:space:]]+|EC[[:space:]]+|OPENSSH[[:space:]]+)?PRIVATE[[:space:]]+KEY-----'
  'gh[pousr]_[0-9A-Za-z]{20,}'
  'github_pat_[0-9A-Za-z_]{20,}'
  'xox[baprs]-[0-9A-Za-z-]{10,}'
  'AKIA[0-9A-Z]{16}'
  'sk-(?:proj-)?[0-9A-Za-z_-]{20,}'
  'sk_live_[0-9A-Za-z]{16,}'
  'SK[0-9a-fA-F]{32}'
  '(?i)(?:api[_-]?key|client[_-]?secret|access[_-]?token|auth[_-]?token|password)[[:space:]]*[:=][[:space:]]*["'\'' ]?(?!(?:MY|YOUR|EXAMPLE|PLACEHOLDER|DUMMY|TEST)(?:_|-))[0-9A-Za-z_./+=-]{20,}'
)

pattern_args=()
for pattern in "${patterns[@]}"; do
  pattern_args+=(-e "$pattern")
done

rg_args=(--pcre2 --hidden -I -l)
rg_args+=("${pattern_args[@]}")
rg_args+=(
  --glob '!.git/**'
  --glob '!**/build/**'
  --glob '!.gradle/**'
  --glob '!.kotlin/**'
  --glob '!*.apk'
  --glob '!*.aab'
  --glob '!*.jks'
  --glob '!*.keystore'
  --glob '!*.jpg'
  --glob '!*.jpeg'
  --glob '!*.png'
  --glob '!*.webp'
)

failed=0

scan_workspace() {
  local output status
  set +e
  output="$(rg "${rg_args[@]}" . 2>/dev/null)"
  status=$?
  set -e
  if [[ $status -eq 0 ]]; then
    echo "BLOCKED: possible credential material found in these files:" >&2
    printf '%s\n' "$output" >&2
    echo "Values are intentionally redacted. Remove or rotate credentials before continuing." >&2
    failed=1
  elif [[ $status -ne 1 ]]; then
    echo "ERROR: workspace secret scan failed" >&2
    exit "$status"
  fi
}

scan_staged() {
  local status
  set +e
  git diff --cached --no-color --unified=0 --text | rg --pcre2 -q "${pattern_args[@]}"
  status=${PIPESTATUS[1]}
  set -e
  if [[ $status -eq 0 ]]; then
    echo "BLOCKED: possible credential material found in the staged Git diff. Values are redacted." >&2
    failed=1
  elif [[ $status -ne 1 ]]; then
    echo "ERROR: staged Git diff secret scan failed" >&2
    exit "$status"
  fi
}

scan_history() {
  local status
  set +e
  git log --all -p --no-color --text | rg --pcre2 -q "${pattern_args[@]}"
  status=${PIPESTATUS[1]}
  set -e
  if [[ $status -eq 0 ]]; then
    echo "BLOCKED: possible credential material found in Git history. Values are redacted." >&2
    failed=1
  elif [[ $status -ne 1 ]]; then
    echo "ERROR: Git history secret scan failed" >&2
    exit "$status"
  fi
}

if [[ "$mode" == "--workspace" || "$mode" == "--all" ]]; then
  scan_workspace
fi

if [[ "$mode" == "--staged" || "$mode" == "--all" ]]; then
  scan_staged
fi

if [[ "$mode" == "--history" || "$mode" == "--all" ]]; then
  if git rev-parse --verify HEAD >/dev/null 2>&1; then
    scan_history
  fi
fi

if [[ $failed -ne 0 ]]; then
  exit 2
fi

echo "Secret scan passed ($mode)."
