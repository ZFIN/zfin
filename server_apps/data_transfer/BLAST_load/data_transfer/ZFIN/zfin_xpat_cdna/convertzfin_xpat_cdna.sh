#!/bin/tcsh
#
# Each Sunday after zfin finish data transfer in
# get the most up-to-date accession list from almost.
#
#,@BLASTSERVER_BLAST_DATABASE_PATH@/Current/gbk_zf_dna
cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_xpat_cdna/


@BLASTSERVER_XDFORMAT@ -n -o ZFINGenesWithExpression -e @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_xpat_cdna/xdformat_zfin_xpat_cdna.log -I -t "ZFIN cDNA Sequence Set" @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_xpat_cdna/zfin_xpat_cdna_seq.fa

echo "done formatting zfin_xpat_cdna into dbs" 


exit
