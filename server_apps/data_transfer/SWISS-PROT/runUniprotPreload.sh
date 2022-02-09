#!/bin/bash -e
#
# Use environment variable "SKIP_DOWNLOADS" to not to download anything and instead assume files exist locally (will exit if no file exists)
# Use environment variable "SKIP_MANUAL_CHECK" to not to scan uniprot for IDs gone bad
# Use environment variable "SKIP_CLEANUP" to not to remove old files
# Use environment variable "SKIP_SLEEP" to not to sleep for 500 seconds at various parts
# Use environment variable "ARCHIVE_ARTIFACTS" to to save a copy of all generated artifacts in subdirectory called archives/$TIMESTAMP
# Use environment variable "SKIP_PRE_ZFIN_GEN" to skip the step of building pre_zfin.dat. Useful for troubleshooting if a pre_zfin.dat file is already in place.
# Flags are useful for being a good citizen and not putting too much strain on servers.
#
# Can run with those env vars in tcsh like so: (https://stackoverflow.com/questions/5946736/)
#    env SKIP_DOWNLOADS=1 env SKIP_MANUAL_CHECK=1 ./runUniprotPreload.sh
# or
#     ( setenv SKIP_DOWNLOADS 1 ; setenv SKIP_MANUAL_CHECK 1; setenv SKIP_CLEANUP 1 ; ./runUniprotPreload.sh )
#     ( setenv SKIP_MANUAL_CHECK 1 ; setenv SKIP_CLEANUP 1 ; setenv SKIP_SLEEP 1 ; setenv SKIP_PRE_ZFIN_GEN 1 ; setenv SKIP_DOWNLOADS 1 ; setenv ARCHIVE_ARTIFACTS 1; ./runUniprotPreload.sh )
# or for bash:
#     SKIP_MANUAL_CHECK=1 SKIP_CLEANUP=1 SKIP_SLEEP=1 SKIP_PRE_ZFIN_GEN=1 SKIP_DOWNLOADS=1 ARCHIVE_ARTIFACTS=1 ./runUniprotPreload.sh

main() {
echo "#########################################################################"
echo "run pre_loadsp.pl " $(date "+%Y-%m-%d %H:%M:%S")

./pre_loadsp.pl ;

echo "run sp_check.pl " $(date "+%Y-%m-%d %H:%M:%S")

./sp_check.pl ;

echo "/bin/cat prob1 prob2 prob3 prob4 prob5 prob6 prob7 prob8 > allproblems.txt " $(date "+%Y-%m-%d %H:%M:%S")

/bin/cat prob1 prob2 prob3 prob4 prob5 prob6 prob7 prob8 > allproblems.txt ;

echo "run sp_match.pl manuallyCuratedUniProtIDs.txt " $(date "+%Y-%m-%d %H:%M:%S")

./sp_match.pl manuallyCuratedUniProtIDs.txt ;

echo "#########################################################################"
echo "## FINISHED runUniprotPreload.sh "      $(date "+%Y-%m-%d %H:%M:%S")
echo "#########################################################################"
}

main 2>&1 | tee preload_log.txt
