#!/bin/bash
#
# Validate or submit the Alliance FMS file set.
#
#   ./validateAllianceFiles.sh <submit> <release-version>
#
#     submit           "true" submits; anything else validates
#     release-version  e.g. 9.2.0; forms the field name <version>_<TYPE>_ZFIN
#
# Run from this directory: the file names below and the token lookup are both
# resolved relative to the working directory, and the Jenkins job stages the
# gzipped set here before calling it.

main() {
  loadConfig "$1" "$2"

  validateFile ZFIN_1.0.1.4_STR.json.gz SQTR
  validateFile ZFIN_1.0.1.4_HTP_Dataset.json.gz HTPDATASET
  validateFile ZFIN_1.0.1.4_HTP_DatasetSample.json.gz HTPDATASAMPLE
  validateFile ZFIN_1.0.1.4_phenotype.json.gz PHENOTYPE
  validateFile ZFIN_1.0.1.4_expression.json.gz EXPRESSION
  validateFile ZFIN_1.0.1.4_variant.json.gz VARIATION
  validateFile zfin_genes.gff3 GFF
  validateFile ZFIN_1.0.1.4_Reference.json.gz REFERENCE
  validateFile ZFIN_1.0.1.4_Resource.json.gz RESOURCE
  validateFile ZFIN_1.0.1.4_ReferenceExchange.json.gz REF-EXCHANGE

  echo ""

  #Display error if found
  if [ $BUILD_STATUS_CODE -ne 0 ]; then
    echo "---------------"
    echo "FAILED UPLOADS:"
    echo "---------------"
    echo "$FAILED_UPLOADS" | sed 's/, $//' #remove trailing comma
  fi

  echo ""

  exit $BUILD_STATUS_CODE
}

# Read the FMS token from TokenStorage ($TARGETROOT/server_apps/tokens), which
# is outside the git tree. It used to be sourced from authorization.txt in this
# directory -- i.e. a live credential inside the checkout, one `git add -A` away
# from being published.
#
# Still honours an existing authorization.txt if one is present, so an instance
# that has not migrated keeps working; the token file wins when both exist.
readToken() {
  local tokenFile="${TARGETROOT}/server_apps/tokens/alliance-api-token.txt"
  if [ -n "$TARGETROOT" ] && [ -f "$tokenFile" ]; then
    AUTHORIZATION=$(tr -d '\r\n' < "$tokenFile")
    echo "token: read from $tokenFile"
  elif [ -f "authorization.txt" ]; then
    # Legacy location. Shell syntax (AUTHORIZATION=<token>) because it is sourced.
    source authorization.txt
    echo "token: read from ./authorization.txt (legacy -- migrate to TokenStorage)"
  else
    echo "token: NOT FOUND"
  fi
}

loadConfig() {
  readToken

  BASE_URL="https://fms.alliancegenome.org"
#  FOR LOCAL TESTING:
#  BASE_URL="http://localhost:3000"

  RELEASE_VERSION=$2
  BUILD_STATUS_CODE=0
  FAILED_UPLOADS=""
  ENDPOINT=validate
  SUBMITTING="false"
  if [ "$1" == "true" ]; then
        ENDPOINT=submit
        SUBMITTING="true"
        echo "submit files"
  fi

  echo "endpoint: '$ENDPOINT'"
  echo "release version: '$RELEASE_VERSION'"

  # Fail fast rather than uploading nothing and reporting success.
  #
  # Both of these used to be warnings. A missing token meant every request went
  # out as "Bearer " and came back 401; an empty release version skipped the
  # upload block entirely. Either way the job exited 0, so a run that submitted
  # nothing was indistinguishable from one that worked -- which is how this went
  # unnoticed long enough for the token to disappear from the host altogether.
  if [ -z "$RELEASE_VERSION" ]; then
    echo "ERROR: no release version given. Nothing would be uploaded." >&2
    echo "       Set ALLIANCE_RELEASE_VERSION (e.g. 9.2.0) and re-run." >&2
    exit 1
  fi
  if [ -z "$AUTHORIZATION" ]; then
    echo "ERROR: no Alliance FMS token available." >&2
    echo "       Expected \$TARGETROOT/server_apps/tokens/alliance-api-token.txt" >&2
    echo "       containing the bare token (no AUTHORIZATION= prefix)." >&2
    echo "       Write it with: gradle tokenStorage --args=\"write ALLIANCE_API_TOKEN <token>\"" >&2
    exit 1
  fi
}

validateFile() {
  JSON_FILENAME=$1
  FIELD_NAME=$2
  FRIENDLY_FILENAME=$JSON_FILENAME
  TEMP_RESPONSE_FILE=/tmp/agr_upload_response.txt
  EXIT_CODE=0

  #check if validation file exists
  if [ ! -f "$JSON_FILENAME" ]; then
    echo "ERROR: File not found: '$JSON_FILENAME'"
    BUILD_STATUS_CODE=1
    FAILED_UPLOADS="$FRIENDLY_FILENAME (missing), $FAILED_UPLOADS"
    return
  fi

  #Validate file (or submit)
  echo ""
  echo "Validating $FRIENDLY_FILENAME file..."
  echo "curl --silent -H \"Authorization: Bearer AUTHORIZATION\" -X POST \"$BASE_URL/api/data/$ENDPOINT\" -F \"${RELEASE_VERSION}_${FIELD_NAME}_ZFIN=@${JSON_FILENAME}\""

  # Capture the HTTP status as well as the body. The old check grepped the body
  # for '"status":"failed"' and nothing looked at the status code at all, so a
  # 401 (empty body) or a 500 (HTML body) could pass as success. The FMS does
  # return 200 with {"status":"failed"} for content problems, so both checks are
  # needed -- neither alone is sufficient.
  HTTP_CODE=$(curl --silent -o "$TEMP_RESPONSE_FILE" -w '%{http_code}' \
    -H "Authorization: Bearer $AUTHORIZATION" \
    -X POST "$BASE_URL/api/data/$ENDPOINT" \
    -F "${RELEASE_VERSION}_${FIELD_NAME}_ZFIN=@${JSON_FILENAME}")
  CURL_RC=$?
  cat "$TEMP_RESPONSE_FILE"
  echo ""

  if [ $CURL_RC -ne 0 ]; then
    echo "ERROR: curl failed (exit $CURL_RC) for $FRIENDLY_FILENAME"
    EXIT_CODE=1
  elif [ "$HTTP_CODE" == "401" ] || [ "$HTTP_CODE" == "403" ]; then
    # Called out separately: this is a credential problem, not a data problem,
    # and every remaining file will fail the same way.
    echo "ERROR: HTTP $HTTP_CODE for $FRIENDLY_FILENAME -- the FMS rejected the token."
    EXIT_CODE=1
  elif [ "${HTTP_CODE:0:1}" != "2" ]; then
    echo "ERROR: HTTP $HTTP_CODE for $FRIENDLY_FILENAME"
    EXIT_CODE=1
  elif grep -q '"status":"failed"' "$TEMP_RESPONSE_FILE"; then
    echo "ERROR: FMS reported status:failed for $FRIENDLY_FILENAME"
    EXIT_CODE=1
  fi

  rm -f "$TEMP_RESPONSE_FILE"

  #Handle error if found
  if [ $EXIT_CODE -ne 0 ]; then
      echo "ERROR: Failure validating $FRIENDLY_FILENAME file"
      BUILD_STATUS_CODE=$EXIT_CODE
      FAILED_UPLOADS="$FRIENDLY_FILENAME, $FAILED_UPLOADS"
  fi

}

main "$1" "$2"
