#!/usr/bin/env bash
# Builds a signed release of Toolbox and publishes it to the `releases` branch,
# where the installed app picks it up on next launch.
#
# Needs:  TOOLBOX_KEYSTORE_FILE      path to toolbox-release.p12
#         TOOLBOX_KEYSTORE_PASSWORD  its password
#         the Android SDK (scripts/install-android-sdk.sh)
#
# The `releases` branch is rebuilt from scratch on every release and holds only
# the newest and previous APK, so the repo doesn't grow. It is force-pushed.
set -euo pipefail
cd "$(dirname "$0")/.."

REPO="The-Ops-King/app-test"
BRANCH="releases"
APP="toolbox"
RAW="https://raw.githubusercontent.com/$REPO/$BRANCH/$APP"
# Fingerprint of the one signing key. Any other key would break updates on the phone.
EXPECTED_CERT="d930f041069010e3991a53dc1d339ce38104dadec3e06f3b5df1e27183e80a82"

die() { echo "release: $*" >&2; exit 1; }

: "${TOOLBOX_KEYSTORE_FILE:?set TOOLBOX_KEYSTORE_FILE}"
: "${TOOLBOX_KEYSTORE_PASSWORD:?set TOOLBOX_KEYSTORE_PASSWORD}"
[ -f "$TOOLBOX_KEYSTORE_FILE" ] || die "keystore not found at $TOOLBOX_KEYSTORE_FILE"

SDK="${ANDROID_HOME:-$(sed -n 's/^sdk.dir=//p' local.properties 2>/dev/null || true)}"
APKSIGNER="$SDK/build-tools/37.0.0/apksigner"
[ -x "$APKSIGNER" ] || die "Android SDK missing. Run scripts/install-android-sdk.sh"

# Only release code that is committed and pushed to main, so every build can be traced.
git diff --quiet && git diff --cached --quiet || die "uncommitted changes. Commit and push to main first."
git fetch -q origin main
[ "$(git rev-parse HEAD)" = "$(git rev-parse origin/main)" ] || die "HEAD isn't origin/main. Push to main first."
SOURCE_SHA="$(git rev-parse --short HEAD)"

WORK="$(mktemp -d)"
trap 'rm -rf "$WORK" "$WORK.index"' EXIT
mkdir -p "$WORK/$APP"

PREV_CODE=0
if git fetch -q origin "$BRANCH" 2>/dev/null; then
  git archive FETCH_HEAD | tar -x -C "$WORK"
  [ -f "$WORK/$APP/update.json" ] && PREV_CODE="$(jq -r .versionCode "$WORK/$APP/update.json")"
fi
CODE=$((PREV_CODE + 1))
VERSION="1.0.$CODE"
echo "Building $APP $VERSION (versionCode $CODE) from $SOURCE_SHA"

TOOLBOX_VERSION_CODE="$CODE" ./gradlew --quiet -I scripts/maven-mirror.init.gradle testDebugUnitTest assembleRelease
APK="app/build/outputs/apk/release/app-release.apk"

"$APKSIGNER" verify --print-certs "$APK" | grep -q "SHA-256 digest: $EXPECTED_CERT" \
  || die "APK isn't signed with the Toolbox key. Refusing to publish."

# Keep the previous APK: raw.githubusercontent.com caches update.json for a few
# minutes, and a stale copy must still point at a file that exists.
find "$WORK/$APP" -name "$APP-*.apk" ! -name "$APP-$PREV_CODE.apk" -delete
cp "$APK" "$WORK/$APP/$APP-$CODE.apk"
cp "$APK" "$WORK/$APP/$APP.apk" # stable link for a fresh install
jq -n \
  --argjson versionCode "$CODE" \
  --arg versionName "$VERSION" \
  --arg apkUrl "$RAW/$APP-$CODE.apk" \
  --arg sha256 "$(sha256sum "$APK" | cut -d' ' -f1)" \
  --arg source "$SOURCE_SHA" \
  '{versionCode: $versionCode, versionName: $versionName, apkUrl: $apkUrl, sha256: $sha256, source: $source}' \
  > "$WORK/$APP/update.json"

export GIT_INDEX_FILE="$WORK.index"
git --work-tree="$WORK" add -A
TREE="$(git write-tree)"
unset GIT_INDEX_FILE
COMMIT="$(git commit-tree "$TREE" -m "$APP $VERSION from $SOURCE_SHA")"

for delay in 0 2 4 8 16; do
  sleep "$delay"
  git push -q -f origin "$COMMIT:refs/heads/$BRANCH" && break
  [ "$delay" = 16 ] && die "push failed"
done

echo
echo "Published $APP $VERSION"
echo "Fresh install: $RAW/$APP.apk"
echo "Installed phones see the update next time the app opens (GitHub may take up to ~5 minutes)."
