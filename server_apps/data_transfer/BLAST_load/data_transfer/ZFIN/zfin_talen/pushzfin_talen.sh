#!/bin/tcsh
#
# Scp talen sequence 
# update blast db.
# 

#=======================
# Move current to backup
# update current dir
#========================

mv @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_talen/zfin_talen.x* @BLASTSERVER_BLAST_DATABASE_PATH@/Current/

echo "done pushing zfin_talen"
exit
