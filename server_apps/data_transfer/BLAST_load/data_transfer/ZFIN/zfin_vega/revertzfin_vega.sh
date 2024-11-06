#!/bin/tcsh
#
cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_vega

# vega_zfin blast database is named unlike all other zfin dbs.  
# decided to change the naming convention to zfin_vega for all 
# scripts, to be consistant, but leave the blastdb name backwards
# until we know all the places to change it.

rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/vega*
cp -f @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/vega* @BLASTSERVER_BLAST_DATABASE_PATH@/Current

#if (@HOSTNAME@ == genomix.cs.uoregon.edu) then
#
#  @TARGET_PATH@/ZFIN/zfin_vega/distributeToNodeszfin_vega.sh
#
#endif

echo "finished reverting fa file and xd files"

exit
