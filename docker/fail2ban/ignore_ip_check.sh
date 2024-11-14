#!/bin/bash
IP="$1"
HOSTRESULT="$(host -W 1 ${IP})"
REGEX='.*(googlebot\.com\.|google\.com\.)'
if [[ "$HOSTRESULT" =~ $REGEX ]]; then exit 0; else exit 1; fi
