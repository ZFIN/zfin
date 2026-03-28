#!/bin/bash
#
# Lazy extraction shim for large tools in /opt/misc.
#
# Problem: Bowtie and Bowtie2 are ~555MB extracted but only used by a few
# Jenkins jobs. Extracting them at Docker build time bloats every container
# that inherits the base image.
#
# Solution: Keep the zip files compressed in the image (~140MB) and extract
# on first use. This script acts as a "shim" — a lightweight stand-in that
# is symlinked at every expected binary path:
#
#   /opt/misc/bowtie/bowtie        -> /opt/misc/lazy-extract.sh
#   /opt/misc/bowtie/bowtie-build  -> /opt/misc/lazy-extract.sh
#   /opt/misc/bowtie2/bowtie2      -> /opt/misc/lazy-extract.sh
#   /opt/misc/bowtie2/bowtie2-build -> /opt/misc/lazy-extract.sh
#
# When a script calls e.g. /opt/misc/bowtie/bowtie, this shim runs instead.
# It figures out which tool was called by inspecting $0 (the symlink path),
# extracts the corresponding zip, replaces the shim directory with a symlink
# to the extracted directory, and exec's the real binary.
#
# On subsequent calls, /opt/misc/bowtie is now a symlink to the extracted
# directory, so the real binary runs directly — this script is never invoked
# again.

set -eu

# $0 is the symlink path (e.g., /opt/misc/bowtie/bowtie)
CALLED_AS="$0"
SHIM_DIR="$(dirname "$CALLED_AS")"   # e.g., /opt/misc/bowtie
SHIM_NAME="$(basename "$SHIM_DIR")"  # e.g., bowtie
BINARY_NAME="$(basename "$CALLED_AS")" # e.g., bowtie or bowtie-build
MISC_DIR=/opt/misc

# Map shim directory name to archive filename and extracted directory name
case "$SHIM_NAME" in
    bowtie)  ZIP="bowtie-1.3.1-linux-x86_64.zip"; EXTRACTED_DIR="bowtie-1.3.1-linux-x86_64" ;;
    bowtie2) ZIP="bowtie2-2.5.3-linux-x86_64.zip"; EXTRACTED_DIR="bowtie2-2.5.3-linux-x86_64" ;;
    *)       echo "lazy-extract: unknown tool '$SHIM_NAME'" >&2; exit 1 ;;
esac

# Extract on first use only
if [ ! -f "$MISC_DIR/$EXTRACTED_DIR/$BINARY_NAME" ]; then
    echo "First run of $SHIM_NAME: extracting $ZIP..." >&2
    cd "$MISC_DIR"
    unzip -q "$ZIP"
    # Replace the shim directory (containing symlinks to this script)
    # with a symlink to the real extracted directory
    rm -rf "$SHIM_DIR"
    ln -s "$EXTRACTED_DIR" "$SHIM_DIR"
    echo "Done. $SHIM_NAME is ready." >&2
fi

exec "$MISC_DIR/$EXTRACTED_DIR/$BINARY_NAME" "$@"
