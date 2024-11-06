#!/bin/tcsh
#
# TIGR ftp site has all the releases under the same directory.
# We keep the current version number in "tigr.ftp" file, and 
# probe for next release. This scripts reads the "tigr.ftp" and 
# calculate the to-be-downloaded version. After the processing,
# it writes the current version back into "tigr.ftp" file.
#

echo "==| Start of the processTIGR load  |=="

cd @TARGET_PATH@/TIGR

echo "==| Download TIGR |== "
@TARGET_PATH@/TIGR/downloadTIGR.sh

echo "==| assemble TIGR |== "
@TARGET_PATH@/TIGR/assembleTIGR.sh

echo "==| convert TIGR |== "
@TARGET_PATH@/TIGR/convertTIGR.sh

echo "==| push TIGR |== "
@TARGET_PATH@/TIGR/pushTIGR.sh

#if (@HOSTNAME@ == genomix.cs.uoregon.edu) then
#
# @TARGET_PATH@/TIGR/distributeToNodesTIGR.sh
#
#endif

echo "==| Done with processTIGR |== "
