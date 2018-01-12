#!/bin/bash -e 

/local/bin/wget -N ftp://ftp.sanger.ac.uk/pub/vega/zebrafish/zebrafish_VEGA67_67.gff3.gz
/bin/touch process_zebrafish_VEGA67_67.gff3
/bin/rm process_zebrafish_VEGA67_67.gff3

/bin/cp zebrafish_VEGA67_67.gff3.gz process_zebrafish_VEGA67_67.gff3.gz
/local/bin/gunzip process_zebrafish_VEGA67_67.gff3.gz

/bin/sed 's/;.*/;/' process_zebrafish_VEGA67_67.gff3 > processing.tmp && mv processing.tmp process_zebrafish_VEGA67_67.gff3
/bin/sed 's/;/|/g' process_zebrafish_VEGA67_67.gff3 > processing.tmp && mv processing.tmp process_zebrafish_VEGA67_67.gff3
/bin/sed 's/	/|/g' process_zebrafish_VEGA67_67.gff3 > processing.tmp && mv processing.tmp process_zebrafish_VEGA67_67.gff3
/bin/sed '/^#/d' process_zebrafish_VEGA67_67.gff3 > processing.tmp && mv processing.tmp process_zebrafish_VEGA67_67.gff3
/bin/sed '/^$/d' process_zebrafish_VEGA67_67.gff3 > processing.tmp && mv processing.tmp process_zebrafish_VEGA67_67.gff3
/bin/sed 's/ID=//g' process_zebrafish_VEGA67_67.gff3 > processing.tmp && mv processing.tmp process_zebrafish_VEGA67_67.gff3

/private/apps/Informix/informix/bin/dbaccess -a $DBNAME loadVegaStartEnd.sql
