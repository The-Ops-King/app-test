# Toolbox: project notes

Personal Android app. Releases are built in a Claude session and published to the `releases` branch, which the installed app checks for updates.

## Release procedure

1. Make the change and run `./gradlew testDebugUnitTest`.
2. Commit and push to `main` (confirm with the owner that pushing to `main` and `releases` is fine in the current session).
3. Install the Android SDK if needed: `scripts/install-android-sdk.sh <dir outside the repo>`.
4. The signing key is in the owner's Google Drive, folder `Toolbox/signing`:
   - `toolbox-release.p12.base64.txt`: base64 of the PKCS12 keystore (decode with `base64 -d`).
   - `toolbox-release-password.txt`: the keystore password.

   Put the decoded files in a private (`chmod 700`) directory outside the repo.
5. Run `TOOLBOX_KEYSTORE_FILE=<p12> TOOLBOX_KEYSTORE_PASSWORD="$(cat <password file>)" scripts/release.sh`.
6. Report the published version. The app shows "Update now" on next launch; raw.githubusercontent.com can lag a few minutes.

## Rules

- Don't commit the keystore or password, and don't print the password.
- Don't generate a new signing key. If the Drive files are missing, ask the owner. A different key means installed copies must be uninstalled.
- Don't change `applicationId` (`com.theopsking.toolbox`).
- `versionCode` comes from the `releases` branch (last published + 1). Don't set it by hand.
- Text people will read (cards, app text) uses plain wording with no em dashes.
