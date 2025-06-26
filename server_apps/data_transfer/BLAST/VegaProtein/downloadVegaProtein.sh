#!/bin/tcsh

#echo "==| at @BLASTSERVER_FASTA_FILE_PATH@/fasta/VegaProteinProt |=="

cd @TARGET_PATH@/VegaProtein/ 

rm -f Danio*

wget -Nq "ftp://ftp.ensembl.org/pub/vega/zebrafish/pep/Danio_rerio.VEGA67.pep.all.fa.gz";

set count=`/bin/ls -l Danio* | /bin/wc -l`

if ($count>1) then
    rm `ls -t Danio* | awk 'NR>1'`;
endif 

cp *.pep.all.fa.gz downloaded.gz

echo "== Unzip file == "
gunzip downloaded.gz

cp downloaded vegaprotein.fa ;

@TARGET_PATH@/VegaProtein/deflineSwitch.pl vegaprotein.fa > vegaprotein_zf.fa

rm -f downloaded;
rm vegaprotein.fa;


exit
