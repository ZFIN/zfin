#!/bin/tcsh
#
# Each Sunday after zfin finish data transfer in
# get the most up-to-date accession list from almost.
#
cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_cdna/

if (@HOSTNAME@ == genomix.cs.uoregon.edu) then
 echo "distributes to nodes zfin_cdna"

 foreach i (001  003 004 005)
   rsync -avz -e ssh @BLASTSERVER_BLAST_DATABASE_PATH@/Current/zfin_cdna_seq.* node${i}:@BLASTSERVER_BLAST_DATABASE_PATH@/Current
 end

endif

exit;
