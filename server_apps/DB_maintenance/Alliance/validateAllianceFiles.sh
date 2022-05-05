#!/bin/bash

main() {
  loadConfig $1 $2

  validateFile ZFIN_1.0.1.4_basicGeneInformation.json.gz BGI
  validateFile ZFIN_1.0.1.4_allele.json.gz ALLELE
  validateFile ZFIN_1.0.1.4_STR.json.gz SQTR
  validateFile ZFIN_1.0.1.4_disease.daf.json.gz DAF
  validateFile ZFIN_1.0.1.4_Construct.json.gz CONSTRUCT
  validateFile ZFIN_1.0.1.4_HTP_Dataset.json.gz HTPDATASET
  validateFile ZFIN_1.0.1.4_HTP_DatasetSample.json.gz HTPDATASAMPLE
  validateFile ZFIN_1.0.1.4_phenotype.json.gz PHENOTYPE
  validateFile ZFIN_1.0.1.4_AGM.json.gz AGM
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

loadConfig() {
  if [ -f "authorization.txt" ]
  then
    # load the AUTHORIZATION token from external file
    source authorization.txt
  else
    echo "No authorization.txt file found"
  fi

  BASE_URL="https://fms.alliancegenome.org"
#  FOR LOCAL TESTING:
#  BASE_URL="http://localhost:3000"

  RELEASE_VERSION=$2
  BUILD_STATUS_CODE=0
  FAILED_UPLOADS=""
  ENDPOINT=validate
  if [ "$1" == "true" ]; then
        ENDPOINT=submit
        echo "submit files"
  fi

  echo "endpoint: '$ENDPOINT'"
  echo "release version: '$RELEASE_VERSION'"
  if [ -z "$RELEASE_VERSION" ]; then
    echo "ERROR: No release version provided!"
    exit 1
  fi

}

validateFile() {
  JSON_FILENAME=$1
  FIELD_NAME=$2
  FRIENDLY_FILENAME=$JSON_FILENAME
  TEMP_RESPONSE_FILE=/tmp/agr_upload_response.txt
  FILE_EXISTS="true"

  #check if validation file exists
  ls "$JSON_FILENAME" > /dev/null
  if [ $? -ne 0 ]; then
    echo "ERROR: File not found: '$JSON_FILENAME'"
    FILE_EXISTS="false"
    EXIT_CODE=1
  fi

  if [ $FILE_EXISTS == "true" ]; then
    #Validate file (or submit)
    echo ""
    echo "Validating $FRIENDLY_FILENAME file..."
    echo "curl --silent -H \"Authorization: Bearer AUTHORIZATION\" -X POST \"$BASE_URL/api/data/$ENDPOINT\" -F \"${RELEASE_VERSION}_${FIELD_NAME}_ZFIN=@${JSON_FILENAME}\""
    curl --silent -H "Authorization: Bearer $AUTHORIZATION" -X POST "$BASE_URL/api/data/$ENDPOINT" -F "${RELEASE_VERSION}_${FIELD_NAME}_ZFIN=@${JSON_FILENAME}" | tee $TEMP_RESPONSE_FILE

    #Check server response for failure
    #If server set response code to an error status code in the event of status:failed, we could get curl exit code, but it currently sends back a 200
    grep -qv '"status":"failed"' $TEMP_RESPONSE_FILE
    EXIT_CODE=$?
    rm $TEMP_RESPONSE_FILE

    echo ""
  fi

  #Handle error if found
  if [ $EXIT_CODE -ne 0 ]; then
      echo "ERROR: Failure validating $FRIENDLY_FILENAME file"
      BUILD_STATUS_CODE=$EXIT_CODE
      FAILED_UPLOADS="$FRIENDLY_FILENAME, $FAILED_UPLOADS"
  fi

}

main $1 $2