#!/bin/bash -e
#
# Each Sunday after zfin finish data transfer in
# get the most up-to-date accession list from almost.
#

source "../config.sh"

#cd $BLAST_PATH/fasta/ZFIN/zfin_xpat_cdna

#=======================
# Move current to backup
# update current dir
#========================

#mv -f $BLAST_PATH/fasta/ZFIN/zfin_xpat_cdna/*.fa $BLAST_PATH/fasta/ZFIN/Backup
#mv -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/ZFINGenesWithExpression.xn* @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/

#===============
# Download files
#===============

wget -q "http://zfin.org/action/blast/blast-files?action=GENBANK_XPAT_CDNA" -O zfin_gene_xpat_cdna_acc.unl

if [ -f zfin_gene_xpat_cdna_acc.unl ]; then
  log_message "Successfully downloaded XPAT cDNA accessions ($(wc -l <zfin_gene_xpat_cdna_acc.unl) lines)"
else
  error_exit "file zfin_gene_xpat_cdna_acc.unl is empty, not copying."
fi

echo "done downloading zfin_gene_xpat_cdna_acc.unl"

exit
