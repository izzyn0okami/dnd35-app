#!/bin/sh
# Bootstrap launcher for this project. Downloads the official Gradle distribution
# on first use; it does not require gradle-wrapper.jar.
set -eu
GRADLE_VERSION="9.3.1"
GRADLE_HOME_BASE="${GRADLE_USER_HOME:-$HOME/.gradle}/manual-distributions"
GRADLE_DIR="${GRADLE_HOME_BASE}/gradle-${GRADLE_VERSION}"
GRADLE_BIN="${GRADLE_DIR}/bin/gradle"
DIST_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
if [ ! -x "$GRADLE_BIN" ]; then
  mkdir -p "$GRADLE_HOME_BASE"
  ZIP_PATH="${GRADLE_HOME_BASE}/gradle-${GRADLE_VERSION}-bin.zip"
  TMP_DIR="${GRADLE_HOME_BASE}/.gradle-${GRADLE_VERSION}-extract"
  echo "Gradle ${GRADLE_VERSION} não encontrado. Baixando distribuição oficial..."
  if command -v curl >/dev/null 2>&1; then
    curl -fL --retry 3 "$DIST_URL" -o "$ZIP_PATH"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$ZIP_PATH" "$DIST_URL"
  else
    echo "Erro: instale curl ou wget para baixar o Gradle." >&2; exit 1
  fi
  rm -rf "$TMP_DIR"; mkdir -p "$TMP_DIR"
  if command -v unzip >/dev/null 2>&1; then
    unzip -q "$ZIP_PATH" -d "$TMP_DIR"
  elif command -v jar >/dev/null 2>&1; then
    (cd "$TMP_DIR" && jar xf "$ZIP_PATH")
  else
    echo "Erro: instale unzip ou disponibilize o comando jar do JDK." >&2; exit 1
  fi
  rm -rf "$GRADLE_DIR"
  mv "$TMP_DIR/gradle-${GRADLE_VERSION}" "$GRADLE_DIR"
  rm -rf "$TMP_DIR"
fi
exec "$GRADLE_BIN" "$@"
