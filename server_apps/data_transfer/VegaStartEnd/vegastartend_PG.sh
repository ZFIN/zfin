#!/bin/bash -e 

/local/bin/wget -N ftp://ftp.sanger.ac.uk/pub/vega/zebrafish/zebrafish_VEGA59_59.gff3.gz
/bin/touch process_zebrafish_VEGA59_59.gff3
/bin/rm process_zebrafish_VEGA59_59.gff3

/bin/cp zebrafish_VEGA59_59.gff3.gz process_zebrafish_VEGA59_59.gff3.gz
/local/bin/gunzip process_zebrafish_VEGA59_59.gff3.gz

/bin/sed 's/;.*/;/' process_zebrafish_VEGA59_59.gff3 > processing.tmp && mv processing.tmp process_zebrafish_VEGA59_59.gff3
/bin/sed 's/;/|/g' process_zebrafish_VEGA59_59.gff3 > processing.tmp && mv processing.tmp process_zebrafish_VEGA59_59.gff3
/bin/sed 's/	/|/g' process_zebrafish_VEGA59_59.gff3 > processing.tmp && mv processing.tmp process_zebrafish_VEGA59_59.gff3
/bin/sed '/^#/d' process_zebrafish_VEGA59_59.gff3 > processing.tmp && mv processing.tmp process_zebrafish_VEGA59_59.gff3
/bin/sed '/^$/d' process_zebrafish_VEGA59_59.gff3 > processing.tmp && mv processing.tmp process_zebrafish_VEGA59_59.gff3
/bin/sed 's/ID=//g' process_zebrafish_VEGA59_59.gff3 > processing.tmp && mv processing.tmp process_zebrafish_VEGA59_59.gff3

${PGBINDIR}/psql $DBNAME < loadVegaStartEnd.sql
