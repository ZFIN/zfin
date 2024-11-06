#!/bin/tcsh
#
# Push processed Ensembl files to the nodes if on genomix.
#

if ({@HOSTNAME@} == genomix.cs.uoregon.edu) then

 foreach i (001  003 004 005)
   rsync -avz -e ssh @BLASTSERVER_BLAST_DATABASE_PATH@/Current/ensembl_zf* node${i}:@BLASTSERVER_BLAST_DATABASE_PATH@/Current
 end

endif

touch "@BLASTSERVER_FASTA_FILE_PATH@/fasta/ensembl.ftp"

echo "== Finish Distribute To Nodes Ensembl =="
exit
