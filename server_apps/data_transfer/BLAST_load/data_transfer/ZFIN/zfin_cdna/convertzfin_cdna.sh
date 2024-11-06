#!/bin/tcsh
#
# Each Sunday after zfin finish data transfer in
# get the most up-to-date accession list from almost.
#

cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_cdna/

rm -f @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_cdna/vega_zfin.fa

@BLASTSERVER_XDFORMAT@ -n -r @BLASTSERVER_BLAST_DATABASE_PATH@/Current/vega_zfin > @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_cdna/vega_zfin.fa

cat @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_cdna/vega_zfin.fa >> @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_cdna/zfin_cdna_seq.fa

@BLASTSERVER_XDFORMAT@ -n -o zfin_gb_seq -e @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_cdna/xdformat_zfin_gb_seq.log -I  -t "ZFIN GenBank Sequence Set" @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_cdna/zfin_gb_seq.fa

 # vega transcripts defline is like >tpe|OTTDART00000031867|OTTDARG00000016933|ZDB-GENE-000616-16
 # if using -Ttpe, OTTDART and OTTDARG ids would be indexed but not gene ids, without -T, all indexed

@BLASTSERVER_XDFORMAT@ -n -o zfin_cdna_seq -e @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_cdna/xdformat_zfin_cdna.log -I -t "ZFIN cDNA Sequence Set" @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_cdna/zfin_cdna_seq.fa

echo "done formatting zfin_cdna into dbs" 


exit
