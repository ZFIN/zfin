#!/bin/tcsh
#
# Each Sunday after zfin finish data transfer in
# get the most up-to-date accession list from almost.
#

cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_cdna

#===============
# Download files
#===============

/local/bin/wget -q "http://@WEBHOST_HOSTNAME@/action/blast/blast-files?action=GENBANK_CDNA" -O @WEBHOST_FASTA_FILE_PATH@/zfin_genbank_cdna_acc.unl

if ( `cat @WEBHOST_FASTA_FILE_PATH@/zfin_genbank_cdna_acc.unl` == '' ) then
echo "file @WEBHOST_FASTA_FILE_PATH@/zfin_genbank_cdna_acc.unl is empty, not copying."
else
cp  @WEBHOST_FASTA_FILE_PATH@/zfin_genbank_cdna_acc.unl @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_cdna/
endif

/local/bin/wget -q "http://@WEBHOST_HOSTNAME@/action/blast/blast-files?action=GENBANK_ALL" -O @WEBHOST_FASTA_FILE_PATH@/zfin_genbank_acc.unl

if ( `cat @WEBHOST_FASTA_FILE_PATH@/zfin_genbank_acc.unl` == '' ) then
echo "file @WEBHOST_FASTA_FILE_PATH@/zfin_genbank_acc.unl is empty, not copying."
else
cp  @WEBHOST_FASTA_FILE_PATH@/zfin_genbank_acc.unl @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_cdna/
endif



echo "done downloading zfin_genbank_acc.unl zfin_genbank_cdna_acc.unl"
exit
