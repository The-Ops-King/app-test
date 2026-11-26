#!/usr/bin/env bash
# Installs the Android SDK packages this project needs and points Gradle at them.
# Safe to re-run. Usage: scripts/install-android-sdk.sh [sdk-dir]
set -euo pipefail
cd "$(dirname "$0")/.."

SDK_DIR="${1:-${ANDROID_HOME:-$HOME/android-sdk}}"
CLT_ZIP="commandlinetools-linux-16111833_latest.zip"
PACKAGES=("platforms;android-37.2" "build-tools;37.0.0" "platform-tools")

if [ ! -x "$SDK_DIR/cmdline-tools/latest/bin/sdkmanager" ]; then
  echo "Downloading Android command-line tools..."
  tmp="$(mktemp -d)"
  curl -fsSL -o "$tmp/clt.zip" "https://dl.google.com/android/repository/$CLT_ZIP"
  unzip -q "$tmp/clt.zip" -d "$tmp"
  mkdir -p "$SDK_DIR/cmdline-tools"
  rm -rf "$SDK_DIR/cmdline-tools/latest"
  mv "$tmp/cmdline-tools" "$SDK_DIR/cmdline-tools/latest"
  rm -rf "$tmp"
fi

echo "Installing SDK packages..."
yes | "$SDK_DIR/cmdline-tools/latest/bin/sdkmanager" --sdk_root="$SDK_DIR" "${PACKAGES[@]}" > /dev/null

echo "sdk.dir=$SDK_DIR" > local.properties
echo "Android SDK ready at $SDK_DIR"
