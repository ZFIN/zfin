#!/bin/tcsh
#
# Each Sunday after zfin finish data transfer in
# get the most up-to-date accession list from almost.
#

cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_xpat_cdna

#=======================
# Move current to backup
# update current dir
#========================

mv -f @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_xpat_cdna/*.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/Backup
mv -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/ZFINGenesWithExpression.xn* @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/

#===============
# Download files
#===============

/local/bin/wget -q "http://@WEBHOST_HOSTNAME@/action/blast/blast-files?action=GENBANK_XPAT_CDNA" -O @WEBHOST_FASTA_FILE_PATH@/genomix/zfin_xpat_cdna/zfin_gene_xpat_cdna_acc.unl

if ( `cat @WEBHOST_FASTA_FILE_PATH@/genomix/zfin_xpat_cdna/zfin_gene_xpat_cdna_acc.unl` == '' ) then
echo "file @WEBHOST_FASTA_FILE_PATH@/genomix/zfin_xpat_cdna/zfin_gene_xpat_cdna_acc.unl is empty, not copying."
else
cp @WEBHOST_FASTA_FILE_PATH@/genomix/zfin_xpat_cdna/zfin_gene_xpat_cdna_acc.unl @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_xpat_cdna/;
endif


echo "done downloading zfin_genbank_xpat_cdna_acc.unl"

exit
