# Toolbox

A native Android app (Kotlin + Jetpack Compose) for one person's phone. It holds small mini apps, installs straight from this repo, and updates itself. It never goes through the Play Store.

## How updates work

`scripts/release.sh` builds a signed APK and force-pushes it to the `releases` branch with an `update.json` next to it. When the app opens, it reads `releases/toolbox/update.json` from raw.githubusercontent.com. If that version is newer, it shows an "Update now" banner, downloads the APK, checks its SHA-256 and installs it with `PackageInstaller`.

The `releases` branch is rebuilt on every release and only holds the newest and previous APK. Don't commit to it by hand.

Fresh install link: https://raw.githubusercontent.com/The-Ops-King/app-test/releases/toolbox/toolbox.apk

## Signing key

Every release must be signed with the same key, or installed copies refuse the update. The key is never committed. It lives in the owner's Google Drive (`Toolbox/signing`), which is also the backup. `scripts/release.sh` checks the certificate fingerprint and refuses to publish an APK signed with anything else.

## Adding a mini app

1. Write its screen under `app/src/main/java/com/theopsking/toolbox/`.
2. Add an entry to `miniApps` in `MiniApps.kt`.

## Off the Scale cards

Cards live in `app/src/main/assets/off_the_scale/prompts.json`. Each scale is either `{"label": "useful"}`, which shows as "Least useful" / "Most useful", or `{"low": "Cutest", "high": "Scariest"}`. The unit tests check that every card is a question with exactly three scales.

## Building locally

```
scripts/install-android-sdk.sh
./gradlew testDebugUnitTest assembleDebug
```
