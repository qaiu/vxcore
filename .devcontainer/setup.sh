#!/usr/bin/env bash
set -euo pipefail

# Ensure Java 17 and Node are available in the dev container.
echo "[setup] Ensuring Java 17 and Node.js are installed..."

# Prefer devcontainer features-provided java; fallback to apt if missing.
JAVA_OK=false
if command -v java >/dev/null 2>&1; then
  JAVA_VER=$(java -version 2>&1 | head -n1 | sed -E 's/.*"([0-9]+)\..*/\1/')
  if [ "$JAVA_VER" -ge 17 ]; then
    JAVA_OK=true
  fi
fi

if [ "$JAVA_OK" = false ]; then
  echo "[setup] Installing OpenJDK 17 via apt..."
  sudo apt-get update -y
  sudo apt-get install -y openjdk-17-jdk
  sudo update-alternatives --install /usr/bin/java java /usr/lib/jvm/java-17-openjdk-amd64/bin/java 1710 || true
  sudo update-alternatives --install /usr/bin/javac javac /usr/lib/jvm/java-17-openjdk-amd64/bin/javac 1710 || true
fi

# Verify versions
java -version
node -v || true
npm -v || true

# Optional: pre-warm Maven repository for faster builds
if command -v mvn >/dev/null 2>&1; then
  echo "[setup] Pre-warming Maven repo (validate)..."
  mvn -q -DskipTests -Dspotless.apply.skip -Dspotbugs.skip -Dpmd.skip -Dcheckstyle.skip -Ddependency-check.skip=true validate || true
fi

echo "[setup] Done."
