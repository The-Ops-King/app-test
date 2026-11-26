# Toolbox

A native Android app (Kotlin + Jetpack Compose) that holds small mini apps. It installs from GitHub Releases and updates itself.

## How releases work

Every push to `main` runs `.github/workflows/release.yml`, which:

1. runs the unit tests,
2. builds a release APK signed with the key in the repository secrets,
3. publishes a GitHub Release with `toolbox.apk` and `update.json`.

The app reads `releases/latest/download/update.json` when it opens. If the version is newer, it shows an "Update now" banner, downloads the APK, checks its SHA-256 and installs it with `PackageInstaller`.

`versionCode` is the commit count on `main`, so it only goes up. Don't rewrite `main`'s history or force-push, or the count can go backwards and phones will refuse the update.

## Signing key

Two repository secrets are required:

| Secret | Contents |
| --- | --- |
| `TOOLBOX_KEYSTORE_BASE64` | the PKCS12 keystore, base64 encoded |
| `TOOLBOX_KEYSTORE_PASSWORD` | its password (the key alias is `toolbox`) |

Keep a backup of the keystore and password outside GitHub. If they're lost, installed copies can't be updated. You'd have to uninstall and reinstall.

## Adding a mini app

1. Write its screen under `app/src/main/java/com/theopsking/toolbox/`.
2. Add an entry to `miniApps` in `MiniApps.kt`.

## Off the Scale cards

Cards live in `app/src/main/assets/off_the_scale/prompts.json`. Each scale is either `{"label": "useful"}`, which shows as "Least useful" / "Most useful", or `{"low": "Cutest", "high": "Scariest"}`. The unit tests check that every card has a question and exactly three scales.

## Building locally

Needs JDK 17+ and the Android SDK (platform `android-37.2`, build tools `37.0.0`).

```
./gradlew testDebugUnitTest assembleDebug
```
