#!/bin/bash -e
#
# Each Sunday after zfin finish data transfer in
# get the most up-to-date accession list from almost.
#

source "../config.sh"


#=======================
# Move current to backup
# update current dir
#========================
cp -f $BLAST_PATH/Current/zfin_gb_* $BLAST_PATH/Backup/
mv -f zfin_gb_seq.x* $BLAST_PATH/Current

cp -f $BLAST_PATH/Current/zfin_cdna_seq* $BLAST_PATH/Backup/
mv -f zfin_cdna_seq.x* $BLAST_PATH/Current

#if ($HOSTNAME == genomix.cs.uoregon.edu) then
# $TARGET_PATH/ZFIN/zfin_cdna/distributeToNodeszfin_cdna.sh
#endif

log_message "done with pushzfin_cdna.sh" ;
exit
