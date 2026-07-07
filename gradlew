#!/usr/bin/env bash

# Simple wrapper to find and run gradle if gradlew is not fully present
if command -v gradle >/dev/null 2>&1; then
  gradle "$@"
else
  # Try to find gradle in common locations
  for dir in /opt/gradle/bin /usr/local/bin /usr/bin; do
    if [ -x "$dir/gradle" ]; then
      "$dir/gradle" "$@"
      exit $?
    fi
  done
  echo "Error: gradle command not found."
  exit 1
fi
