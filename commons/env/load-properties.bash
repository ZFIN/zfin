#!/usr/bin/env bash
#
# Load ZFIN properties as environment variables
#
# Usage:
#   source load-properties.bash [properties_file]
#
# If no properties file is specified, uses $SOURCEROOT/home/WEB-INF/zfin.properties
# Only exports properties listed in env-exports.properties (if it exists)
#

set -e

# Determine script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Determine SOURCEROOT (use env var if set, otherwise derive from script location)
if [ -z "$SOURCEROOT" ]; then
    # Script is in commons/env/, so SOURCEROOT is two levels up
    SOURCEROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
fi

# Properties file: use argument if provided, otherwise default location
PROPERTIES_FILE="${1:-$SOURCEROOT/home/WEB-INF/zfin.properties}"

# Include list file (controls which properties get exported)
INCLUDE_LIST_FILE="$SCRIPT_DIR/env-exports.properties"

# Generate properties file if it doesn't exist
if [ ! -f "$PROPERTIES_FILE" ]; then
    echo "Properties file '$PROPERTIES_FILE' not found. Attempting to generate..."

    GROOVY_SCRIPT="$SOURCEROOT/commons/env/PropertiesProcessor.groovy"
    YAML_CONFIG="$SOURCEROOT/commons/env/all-properties.yml"

    if [ ! -f "$GROOVY_SCRIPT" ]; then
        echo "Error: PropertiesProcessor.groovy not found at $GROOVY_SCRIPT"
        exit 1
    fi

    # Find groovy executable
    GROOVY_BIN=$(command -v groovy 2>/dev/null || echo "/opt/misc/groovy/bin/groovy")
    if [ ! -x "$GROOVY_BIN" ]; then
        echo "Error: groovy executable not found"
        exit 1
    fi

    "$GROOVY_BIN" "$GROOVY_SCRIPT" -c "$YAML_CONFIG" -o "$PROPERTIES_FILE"
fi

if [ ! -f "$PROPERTIES_FILE" ]; then
    echo "Error: Failed to generate properties file"
    exit 1
fi

echo "Loading properties from $PROPERTIES_FILE..."

# Read and export properties
while IFS='=' read -r key value; do
    # Skip comments and empty lines
    [[ "$key" =~ ^[[:space:]]*# ]] && continue
    [[ -z "$key" ]] && continue

    # If include list exists, only export listed properties
    if [ -f "$INCLUDE_LIST_FILE" ]; then
        grep -q "^${key}$" "$INCLUDE_LIST_FILE" || continue
    fi

    export "$key=$value"
done < "$PROPERTIES_FILE"
