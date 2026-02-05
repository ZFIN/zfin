#!/usr/bin/env bash

# Path to the properties file
PROPERTIES_FILE="/opt/zfin/source_roots/zfin.org/home/WEB-INF/zfin.properties"
INCLUDE_LIST_FILE="/opt/zfin/source_roots/zfin.org/commons/env/env-exports.properties"

# Check if file exists
if [ ! -f "$PROPERTIES_FILE" ]; then
    echo "Error: Properties file '$PROPERTIES_FILE' not found."
    echo "Attempting to generate file."
    /opt/misc/groovy/bin/groovy /opt/zfin/source_roots/zfin.org/commons/env/PropertiesProcessor.groovy -o $PROPERTIES_FILE
fi

if [ ! -f "$PROPERTIES_FILE" ]; then
    echo "Failed to generate properties file"
    exit 1
fi

echo "Loading properties from $PROPERTIES_FILE..."
LOADED_PROPERTIES=""

# Read line by line, skipping comments and empty lines
while IFS='=' read -r key value; do
    # Skip lines starting with #
    if [[ $key =~ ^\s*# ]] || [[ -z $key ]]; then
        continue
    fi

    # Check if the key is in the include list
    if ! grep -q "^$key$" "$INCLUDE_LIST_FILE"; then
        continue
    fi

    # Export the variable
    export "$key=$value"
    LOADED_PROPERTIES+="$key "
done < "$PROPERTIES_FILE"
