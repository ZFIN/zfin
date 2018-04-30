#!/bin/bash -e

echo "#########################################################################"

echo "run pre_loadsp.pl"

pre_loadsp.pl ;

echo "run sp_check.pl"                                                        

sp_check.pl ;

echo "/bin/cat prob1 prob2 prob3 prob4 prob5 prob6 prob7 prob8 > allproblems.txt"

/bin/cat prob1 prob2 prob3 prob4 prob5 prob6 prob7 prob8 > allproblems.txt ;

echo "run sp_match.pl manuallyCuratedUniProtIDs.txt"   

sp_match.pl manuallyCuratedUniProtIDs.txt ;


