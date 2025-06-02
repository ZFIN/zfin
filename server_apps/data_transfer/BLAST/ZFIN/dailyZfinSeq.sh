#!/bin/bash -e
#
# cp over/process curated blastdbs and/or dbs created by zfin acc 
# nums to genomix.
# 

#source /research/zusers/blast/BLAST_load/properties/current;

echo "process zfin_mrph" ;
#zfin_mrph/processzfin_mrph.sh

echo "";
echo "";

echo "process zfin_publishedProtein" ;
#zfin_publishedProtein/processzfin_publishedProtein.sh

echo "";
echo "";

echo "process zfin_unreleasedProtein" ;
#zfin_unreleasedProtein/processzfin_unreleasedProtein.sh

echo "";
echo "";

echo "process zfin_vega" ;
#zfin_vega/processzfin_vega.sh

echo "";
echo "";

echo "process vega_withdrawn" ;
#zfin_vegaWithdrawn/processzfin_vegaWithdrawn.sh

echo "";
echo "";

echo "process zfin_cdna" ;
#zfin_cdna/processzfin_cdna.sh

echo "";
echo "";

echo "process zfin_xpat_cdna" ;
#zfin_xpat_cdna/processzfin_xpat_cdna.sh

echo "";
echo "";

echo "process zfin_unreleasedRNA" ;
#zfin_unreleasedRNA/processzfin_unreleasedRNA.sh

echo "";
echo "";

echo "process zfin_microRNA" ;
#zfin_microRNA/processzfin_microRNA.sh

echo "";
echo "";

echo "process zfin_genomicDNA" ;
zfin_genomicDNA/processzfin_genomicDNA.sh

echo "";
echo "";

echo "process zfin_publishedRNA" ;
#zfin_publishedRNA/processzfin_publishedRNA.sh

echo "";
echo "";

echo "process zfin_crispr" ;
#zfin_crispr/processzfin_crispr.sh

echo "";
echo "";

echo "process zfin_talen" ;
#zfin_talen/processzfin_talen.sh

echo "";
echo "";



exit
