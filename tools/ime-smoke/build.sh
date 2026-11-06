#!/usr/bin/env bash
# SPDX-License-Identifier: GPL-3.0-only
set -euo pipefail
: "${ANDROID_HOME:?Set ANDROID_HOME to an Android SDK}"
: "${JAVA_HOME:?Set JAVA_HOME to a JDK}"
if [[ $# != 1 ]]; then
    echo "Usage: $0 /absolute/path/to/fixture.apk" >&2
    exit 2
fi
fixture_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
work_dir=$(mktemp -d)
trap 'rm -rf -- "$work_dir"' EXIT
build_tools="$ANDROID_HOME/build-tools/36.0.0"
android_jar="$ANDROID_HOME/platforms/android-37.2/android.jar"
mkdir -p "$work_dir/classes" "$work_dir/dex"
"$JAVA_HOME/bin/javac" --release 17 -classpath "$android_jar" -d "$work_dir/classes" "$fixture_dir/FixtureActivity.java"
"$build_tools/d8" --lib "$android_jar" --min-api 29 --output "$work_dir/dex" \
    "$work_dir/classes/io/github/mixxermannipro/satzfink/smoke/FixtureActivity.class"
"$build_tools/aapt2" link -I "$android_jar" --manifest "$fixture_dir/AndroidManifest.xml" \
    --min-sdk-version 29 --target-sdk-version 34 -o "$work_dir/raw.apk"
"$JAVA_HOME/bin/jar" --update --file "$work_dir/raw.apk" -C "$work_dir/dex" classes.dex
"$build_tools/zipalign" -f 4 "$work_dir/raw.apk" "$work_dir/aligned.apk"
"$build_tools/apksigner" sign --ks "${ANDROID_USER_HOME:-$HOME/.android}/debug.keystore" \
    --ks-pass pass:android --out "$1" "$work_dir/aligned.apk"
