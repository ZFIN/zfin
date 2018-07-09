#!/bin/bash -e

echo "#########################################################################"

echo "run pre_loadsp_PG.pl"

pre_loadsp_PG.pl ;

echo "run sp_check_PG.pl"                                                        

sp_check_PG.pl ;

echo "/bin/cat prob1 prob2 prob3 prob4 prob5 prob6 prob7 prob8 > allproblems.txt"

/bin/cat prob1 prob2 prob3 prob4 prob5 prob6 prob7 prob8 > allproblems.txt ;

echo "run sp_match_PG.pl manuallyCuratedUniProtIDs.txt"   

sp_match_PG.pl manuallyCuratedUniProtIDs.txt ;


