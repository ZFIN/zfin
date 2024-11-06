#!/bin/tcsh
#
# Each Sunday after zfin finish data transfer in
# get the most up-to-date accession list from almost.
#

cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_xpat_cdna/

mv @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_xpat_cdna/ZFINGenesWithExpression.x* @BLASTSERVER_BLAST_DATABASE_PATH@/Current


#if (@HOSTNAME@ == genomix.cs.uoregon.edu) then
# @TARGET_PATH@/ZFIN/zfin_xpat_cdna/distributeToNodeszfin_xpat_cdna.sh
#endif

echo "done with pushzfin_xpat_cdna.sh" 
exit
