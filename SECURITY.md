# Lighthouse security boundaries

## Mobile client

- Never compile a durable Gemini API key, service-account credential, OAuth client secret, or App Check debug token into the APK.
- Call Gemini through Firebase AI Logic with App Check enforced. Use the debug provider only in debug builds; production builds use Play Integrity.
- Keep `google-services.json`, `.env`, `local.properties`, keystores, and signing material outside Git and checkpoint archives.
- A Maps SDK key is a client identifier and will be recoverable from an APK. Restrict it in Google Cloud to this Android package, approved SHA-1 certificate fingerprints, and only the required Maps APIs.

## Backend and cloud

- Backend workloads use workload identity / Application Default Credentials. Do not download or commit service-account JSON.
- Store unavoidable server-only secrets in Secret Manager and grant access to the narrowest runtime identity.
- Require authenticated callers and verified App Check tokens before accepting user reports, route mutations, or AI requests.
- Treat self-reports and precise live locations as sensitive. Minimize retention, avoid logging raw coordinates or transcripts, and separate public aggregates from private user records.

## Development gates

- `scripts/security/secret_scan.sh --all` checks the working tree, staged diff, and Git history while printing only affected file names.
- The included GitHub workflow runs the scanner and source-boundary checks on pushes and PRs.
- The optional local pre-commit hook runs the staged scan only after explicitly configuring `core.hooksPath` as described in README. A checked-in hook is not automatically installed.
- Run `scripts/security/test_secret_scan.sh` and `scripts/security/test_android_secret_boundary.sh` after changing build configuration or AI networking.

If a credential is detected or pasted into a public surface, revoke or rotate it first, then remove it from the working tree and history before sharing the repository.
