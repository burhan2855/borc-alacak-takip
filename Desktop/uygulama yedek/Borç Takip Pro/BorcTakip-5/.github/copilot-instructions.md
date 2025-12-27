# Copilot instructions for BorçTakip Android app

This repository is a single-module Android Kotlin + Jetpack Compose application. Use these notes to be immediately productive when authoring or changing code.

- **Big picture**: single app module `:app` using Compose UI, single-activity (`MainActivity`) + `DebtApplication` application class. Core local storage is Room (KSP codegen) and AndroidX Datastore; navigation uses `navigation-compose`; background jobs use WorkManager. See `app/src/main/AndroidManifest.xml` and `app/build.gradle.kts` for anchors.

- **Key files/locations**:
  - App module: [app/build.gradle.kts](../app/build.gradle.kts)
  - Manifest & components: [app/src/main/AndroidManifest.xml](../app/src/main/AndroidManifest.xml)
  - Firebase config: `app/google-services.json` (present)
  - Proguard rules: `app/proguard-rules.pro`
  - KSP caches: `app/build/kspCaches/` (useful when debugging codegen problems)

- **Build / CI / dev commands** (use the Gradle wrapper in repo root):
  - Build debug APK: `./gradlew :app:assembleDebug` (on Windows use `gradlew.bat :app:assembleDebug`)
  - Build release APK (requires signing props): `./gradlew :app:assembleRelease`
  - Run unit tests: `./gradlew :app:testDebugUnitTest`
  - Run instrumentation tests: `./gradlew :app:connectedDebugAndroidTest` or `./gradlew :app:connectedAndroidTest`
  - Lint: `./gradlew :app:lintDebug`

- **Signing / secrets**: release signing is wired to project properties referenced in `app/build.gradle.kts` (e.g. `BORC_TAKIP_STORE_FILE`, `BORC_TAKIP_STORE_PASSWORD`, `BORC_TAKIP_KEY_ALIAS`, `BORC_TAKIP_KEY_PASSWORD`). CI or local dev should set these as Gradle properties or environment variables.

- **Codegen & annotation processing**:
  - Room uses KSP (`ksp("androidx.room:room-compiler:$room_version")`). If model/DAO classes change, run a clean KSP build: `./gradlew :app:clean :app:assembleDebug` and inspect `app/build/ksp` and `app/build/kspCaches` for generated sources.

- **Patterns & conventions to follow** (observable in code):
  - Single-activity Compose app: UI navigation is via Compose `NavHost` (module uses `androidx.navigation:navigation-compose`). Prefer composable-first changes.
  - Persistence: use Room entities/DAOs for relational data and Datastore for lightweight preferences.
  - No DI framework present (no Hilt/ Dagger). Changes should avoid assuming DI unless you add and wire it explicitly.
  - Firebase features present (Auth + Firestore) — `com.google.gms.google-services` plugin is applied; keep server keys out of repo and rely on `google-services.json`.

- **Debugging tips for this repo**:
  - Use `adb logcat` / Android Studio logcat for runtime issues; Compose preview and `debugImplementation` tooling are enabled in `app/build.gradle.kts`.
  - If Room-generated classes are missing or KSP errors appear, check `ksp` plugin version block in `app/build.gradle.kts` and `app/build/ksp` for generated sources.

- **Tests and test runner**: instrumented tests use `androidx.test.runner.AndroidJUnitRunner` (declared in `defaultConfig`). Unit tests are JUnit4.

- **Localization / encoding**: repo contains Turkish strings and non-ASCII characters (e.g., project name and package labels). Preserve UTF-8 encoding in files and commit messages.

- **When making PRs**:
  - Include build commands you ran (Gradle commands), whether you cleaned KSP or cleared caches, and note if release signing was involved.
  - For data model changes (Room entities/DAOs), include a note about migrating local DB versions and KSP regeneration.

If anything above is unclear or you want these instructions to include more examples (code snippets, CI configuration, or common troubleshooting commands), tell me which area to expand. 
