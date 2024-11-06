#!/bin/tcsh
#
# This script downloads and process ReferenceProteome zebrafish file
# and formats it into reference proteome dataset.
#

cd @TARGET_PATH@/ReferenceProteome
 
echo "==| at the @TARGET_PATH@/ReferenceProteome/ ==|"

echo "==| download referenceproteome==|"
 @TARGET_PATH@/ReferenceProteome/downloadReferenceProteome.sh

echo "==| convert referenceproteome==|"
 @TARGET_PATH@/ReferenceProteome/convertReferenceProteome.sh

echo "==| push referenceproteome ==|"
 @TARGET_PATH@/ReferenceProteome/pushReferenceProteome.sh

echo "== Finish ReferenceProteome =="

exit
