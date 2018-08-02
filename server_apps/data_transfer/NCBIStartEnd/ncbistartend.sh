#!/bin/bash -e

/local/bin/wget -N ftp://ftp.ncbi.nlm.nih.gov/genomes/MapView/Danio_rerio/sequence/current/initial_release/seq_gene.md.gz
/bin/touch process_seq_gene.md
/bin/chmod 775 process_seq_gene.md
/bin/rm -f process_seq_gene.md

/bin/cp seq_gene.md.gz process_seq_gene.md.gz
/local/bin/gunzip process_seq_gene.md.gz

/bin/sed '/^#/d' process_seq_gene.md > processing.tmp && mv -f processing.tmp process_seq_gene.md
/bin/sed 's/	/|/g' process_seq_gene.md > processing.tmp && mv -f processing.tmp process_seq_gene.md
/bin/sed 's/$/|/' process_seq_gene.md > processing.tmp && mv -f processing.tmp process_seq_gene.md
/bin/sed '/^7955|Un/d' process_seq_gene.md > processing.tmp && mv -f processing.tmp process_seq_gene.md
/bin/sed 's/GeneID://g' process_seq_gene.md > processing.tmp && mv -f processing.tmp process_seq_gene.md

${PGBINDIR}/psql $DBNAME < loadNCBIStartEnd.sql
