#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
if ! command -v java >/dev/null 2>&1; then
  printf 'Install Java 17 or newer, then run this launcher again.\n' >&2
  exit 1
fi
if [[ ! -f task-manager.jar ]]; then
  printf 'task-manager.jar is missing. Run mvn package, then copy target/task-manager-1.0.0.jar to task-manager.jar.\n' >&2
  exit 1
fi
if [[ -z "${APP_USER:-}" ]]; then read -r -p 'Username: ' APP_USER; fi
if [[ -z "${APP_PASSWORD:-}" ]]; then
  read -r -s -p 'Password (at least 12 characters): ' APP_PASSWORD
  printf '\n'
fi
export APP_USER APP_PASSWORD
printf 'Open http://localhost:%s after the app starts. Press Control+C to stop.\n' "${PORT:-8080}"
exec java -jar task-manager.jar
