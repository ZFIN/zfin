#!/bin/csh

#

echo "BLAST againt Genbank human, mouse, and zebrafish..."


nice +10 /research/zusers/tomc/blast/blastall -p blastn -d /research/zblastdb/db/gb3 -e 1e-50 -o thisse2gb.out -m 8 -i $2


echo "BLAST againt EST zebrafish..."


/research/zusers/tomc/blast/blastall -p blastn -d /research/zblastdb/db/est_zf -e 1e-100 -o thisse2est.out -m 8 -i $2


echo "BLAST againt SwissProt/TrEMBL human, mouse, and zebrafish..."

nice +10 /research/zusers/tomc/blast/blastall -p blastx -d /research/zblastdb/db/sptr3 -e 1e-20 -o thisse2sp.out -m 8 -i $2



echo "PARSE the three output files..."

blast2unl.pl thisse2gb.out 

blast2unl.pl thisse2est.out 

blast2unl.pl -d sp thisse2sp.out 


echo "Find matching ZFIN markers..."

/private/apps/Informix/informix_wavy/bin/dbaccess $1 getMarker.sql


echo "Prepare .txt files for curation ... "

sed 's/\|/      /g' thisse2gb.fin > thisse2gb.txt
sed 's/\|/      /g' thisse2est.fin > thisse2est.txt
sed 's/\|/      /g' thisse2sp.fin > thisse2sp.txt
