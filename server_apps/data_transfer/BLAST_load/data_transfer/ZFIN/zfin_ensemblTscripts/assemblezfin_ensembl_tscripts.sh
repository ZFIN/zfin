#!/bin/tcsh
#
# Each Sunday after zfin finish data transfer in
# get the most up-to-date accession list from almost.
#

 cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_ensembl_tscript/

 #=====================
 # Generate fasta file
 #=====================

@BLASTSERVER_XDGET@ -n -f -e xdget_zfinet_seq.log -o new_zfin_ensembl_tscript.fa  @BLASTSERVER_BLAST_DATABASE_PATH@/Current/ensembl_zf @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_ensembl_tscript/ensemblZfinTscriptsForBlast.txt

exit

