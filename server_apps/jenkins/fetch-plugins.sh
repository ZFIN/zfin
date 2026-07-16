#!/bin/bash
#
# Download the pinned Jenkins plugins listed in a manifest into a target dir,
# verifying each against its sha256. Run at deploy time by the `deploy-plugins`
# Ant target (buildfiles/jenkins.xml), which points target at $JENKINS_HOME/plugins.
# The plugins are the same artifacts the Jenkins update center serves, so they no
# longer need to be committed to git.
#
# Idempotent: a plugin already present with the expected sha256 is left untouched,
# so re-running a deploy neither re-downloads it nor forces Jenkins to re-expand
# it on the next restart. Plugins not listed here (e.g. installed via the UI) are
# left alone.
#
# Usage: fetch-plugins.sh <plugins.txt> <target-dir>
#   plugins.txt lines: "<name> <version> <sha256>"  (# comments / blanks ignored)
#
set -euo pipefail

manifest="${1:?usage: fetch-plugins.sh <plugins.txt> <target-dir>}"
target="${2:?usage: fetch-plugins.sh <plugins.txt> <target-dir>}"
base="https://updates.jenkins.io/download/plugins"

mkdir -p "$target"
downloaded=0
skipped=0
while read -r name version sha256 _rest; do
    [ -z "${name:-}" ] && continue
    case "$name" in \#*) continue ;; esac

    out="$target/$name.jpi"
    if [ -f "$out" ] && echo "$sha256  $out" | sha256sum -c - >/dev/null 2>&1; then
        skipped=$((skipped + 1))
        continue
    fi

    url="$base/$name/$version/$name.hpi"
    echo "Fetching $name $version"
    curl -fsSL "$url" -o "$out"
    echo "$sha256  $out" | sha256sum -c - >/dev/null
    downloaded=$((downloaded + 1))
done < "$manifest"

echo "Plugins: $downloaded downloaded, $skipped already current ($((downloaded + skipped)) total) in $target"
