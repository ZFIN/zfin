#!/bin/bash

main() {
  loadConfig $1 $2

  validateFile basicGeneInformation.json.gz BGI
  validateFile allele.json.gz ALLELE
  validateFile STR.json.gz SQTR
  validateFile disease.json.gz DAF
  validateFile Construct.json.gz CONSTRUCT
  validateFile HTP_Dataset.json.gz HTPDATASET
  validateFile HTP_DatasetSample.json.gz HTPDATASAMPLE
  validateFile phenotype.json.gz PHENOTYPE
  validateFile AGM.json.gz AGM
  validateFile expression.json.gz EXPRESSION
  validateFile variant.json.gz VARIATION
  validateFile zfin_genes.gff3 GFF
  validateFile Reference.json.gz REFERENCE
  validateFile Resource.json.gz RESOURCE
  validateFile ReferenceExchange.json.gz REF-EXCHANGE

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
  BASE_URL="http://localhost:3000"
  ENDPOINT=validate
  RELEASE_VERSION=$2
  BUILD_STATUS_CODE=0

  if [ "$1" == "true" ]; then
        ENDPOINT=submit
        echo "submit files"
  fi

  echo "endpoint: $ENDPOINT"
  echo "release version: $RELEASE_VERSION"
}

validateFile() {
  JSON_FILENAME=$1
  FIELD_NAME=$2
  FRIENDLY_FILENAME=$JSON_FILENAME
  TEMP_RESPONSE_FILE=/tmp/agr_upload_response.txt

  #Validate file (or submit)
  echo ""
  echo "Validating $FRIENDLY_FILENAME file..."
  echo "curl --silent -H \"Authorization: Bearer AUTHORIZATION\" -X POST \"$BASE_URL/api/data/$ENDPOINT\" -F \"${RELEASE_VERSION}_${FIELD_NAME}_ZFIN=@ZFIN_1.0.1.4_${JSON_FILENAME}\""
  curl --silent -H "Authorization: Bearer $AUTHORIZATION" -X POST "$BASE_URL/api/data/$ENDPOINT" -F "${RELEASE_VERSION}_${FIELD_NAME}_ZFIN=@ZFIN_1.0.1.4_${JSON_FILENAME}" | tee $TEMP_RESPONSE_FILE

  #Check server response for failure
  #If server set response code to an error status code in the event of status:failed, we could get curl exit code, but it currently sends back a 200
  grep -qv '"status":"failed"' $TEMP_RESPONSE_FILE
  EXIT_CODE=$?
  rm $TEMP_RESPONSE_FILE

  #Handle error if found
  if [ $EXIT_CODE -ne 0 ]; then
      echo "ERROR: Failure response from server on $FRIENDLY_FILENAME file"
      BUILD_STATUS_CODE=$EXIT_CODE
  fi

}

main $1 $2