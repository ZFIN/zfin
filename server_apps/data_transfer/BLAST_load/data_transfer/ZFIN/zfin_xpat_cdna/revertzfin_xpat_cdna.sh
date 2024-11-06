#!/bin/tcsh
#
# Scp microRNA sequence from embryonix,
# update blast db.
# 

cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_xpat_cdna

# vega_zfin blast database is named unlike all other zfin dbs.  
# decided to change the naming convention to zfin_vega for all 
# scripts, to be consistant, but leave the blastdb name backwards
# until we know all the places to change it.

rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/ZFINGenesWithExpression*
cp -f @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/ZFINGenesWithExpression* @BLASTSERVER_BLAST_DATABASE_PATH@/Current


#if (@HOSTNAME@ == genomix.cs.uoregon.edu) then
#
#  @TARGET_PATH@/ZFIN/zfin_vega/distributeToNodeszfin_xpat_cdna.sh
#
#endif

echo "finished reverting zfin_xpat_cdna"

exit
