#!/bin/tcsh
#
# Scp Crispr sequence 
# update blast db.
# 

#=======================
# Move current to backup
# update current dir
#========================

mv @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_crispr/zfin_crispr.x* @BLASTSERVER_BLAST_DATABASE_PATH@/Current/

echo "done pushing zfin_crispr"
exit
