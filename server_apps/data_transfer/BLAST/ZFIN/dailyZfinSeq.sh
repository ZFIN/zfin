#!/bin/bash -e
#
# cp over/process curated blastdbs and/or dbs created by zfin acc 
# nums to genomix.
# 

#source /research/zusers/blast/BLAST_load/properties/current;
source "config.sh"

log_message "process zfin_mrph" ;
cd zfin_mrph
./processzfin_mrph.sh
cd ..

echo "";

echo "process zfin_publishedProtein" ;
cd zfin_publishedProtein
#./processzfin_publishedProtein.sh
cd ..
echo "";

log_message "process zfin_unreleasedProtein" ;
cd zfin_unreleasedProtein
#./processzfin_unreleasedProtein.sh
cd ..
echo "";

log_message "process zfin_vega" ;
#zfin_vega/processzfin_vega.sh

echo "";

log_message "process vega_withdrawn" ;
#zfin_vegaWithdrawn/processzfin_vegaWithdrawn.sh

echo "";
log_message "process zfin_cdna" ;
cd zfin_cdna
./processzfin_cdna.sh
cd ..

echo "";

log_message "process zfin_xpat_cdna" ;
cd zfin_xpat_cdna
./processzfin_xpat_cdna.sh
cd ..

echo "";

log_message "process zfin_unreleasedRNA" ;
#zfin_unreleasedRNA/processzfin_unreleasedRNA.sh

echo "";

log_message "process zfin_microRNA" ;
#zfin_microRNA/processzfin_microRNA.sh

echo "";

log_message "process zfin_genomicDNA" ;
cd zfin_genomicDNA
./processzfin_genomicDNA.sh
cd ..

echo "";

log_message "process zfin_publishedRNA" ;
#zfin_publishedRNA/processzfin_publishedRNA.sh

echo "";

log_message "process zfin_crispr" ;
cd zfin_crispr
./processzfin_crispr.sh
cd ..

echo "";

log_message "process zfin_talen" ;
cd zfin_talen
./processzfin_talen.sh
cd ..


exit
