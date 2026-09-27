# Lighthouse

Android walking-navigation prototype with Google Maps and a disabled boundary for a future voice companion.

## Development setup

1. Open the project in Android Studio with the Android SDK/API version required by `app/build.gradle.kts`.
2. Provision Firebase for the exact `applicationId` in that file. Place the downloaded client configuration in `app/google-services.json`; it is intentionally ignored by Git.
3. Copy `.env.example` to a local `.env` and supply a Maps SDK key restricted to your Android package, signing certificate and required APIs. The Gemini credential must not be supplied to the Android app; old local entries are explicitly ignored by the Secrets Gradle Plugin.
4. Configure Firebase App Check before enabling a protected Firebase service. The code initializes a debug provider for debug builds and Play Integrity for release builds; initialization alone does not enable console enforcement. Treat debug tokens as credentials and keep them out of shared logs and source.
5. The exported project references a local debug keystore. Configure your own development signing setup, or use Android Studio's normal debug signing. Supply release signing through protected local/CI configuration; never commit signing material.

This export does not include Gradle wrapper scripts/JAR. Restore the wrapper using a trusted Gradle installation matching `gradle/wrapper/gradle-wrapper.properties` before command-line builds.

## Review status

Voice capture and Firebase Live networking are deliberately disabled in this public build. Opening a voice entry point shows an unavailable-state sheet; the app does not request microphone permission or send audio or transcripts. A real voice implementation requires separate permission, lifecycle, privacy, model-availability and release-device App Check testing before it is enabled.

Existing route and safety fixtures remain prototype content outside this security-focused change. This prototype is not ready for production safety use.

## Local checks

```sh
./scripts/security/test_secret_scan.sh
./scripts/security/test_android_secret_boundary.sh
./scripts/security/test_firebase_security_config.sh
./scripts/security/test_android_privacy_boundary.sh
./scripts/security/secret_scan.sh --all
```

The included GitHub workflow runs these checks for pushes and PRs. An optional local staged-file hook can be enabled with `git config core.hooksPath scripts/security` after checking that it will not replace an existing hook setup. Pattern scanning helps catch known credential formats; it is not a complete security audit. See `SECURITY.md` for the configuration and review boundaries.
