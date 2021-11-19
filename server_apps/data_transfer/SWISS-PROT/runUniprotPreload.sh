#!/bin/bash -e

echo "#########################################################################"
echo "run pre_loadsp.pl " $(date "+%Y-%m-%d %H:%M:%S")

pre_loadsp.pl ;

echo "run sp_check.pl " $(date "+%Y-%m-%d %H:%M:%S")

sp_check.pl ;

echo "/bin/cat prob1 prob2 prob3 prob4 prob5 prob6 prob7 prob8 > allproblems.txt " $(date "+%Y-%m-%d %H:%M:%S")

/bin/cat prob1 prob2 prob3 prob4 prob5 prob6 prob7 prob8 > allproblems.txt ;

echo "run sp_match.pl manuallyCuratedUniProtIDs.txt " $(date "+%Y-%m-%d %H:%M:%S")

sp_match.pl manuallyCuratedUniProtIDs.txt ;

echo "#########################################################################"
echo "## FINISHED runUniprotPreload.sh "      $(date "+%Y-%m-%d %H:%M:%S")
echo "#########################################################################"