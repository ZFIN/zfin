#!/bin/tcsh
#
# Download the vega file with corresponding ZFIN info from embryonix.

# vega_zfin blastdb is named unlike all other zfin blastdbs
# make the script naming convention consistant, but use the same
# blastdb name as now until we can figure out all the places to change
# the name.

rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/vega*.xn*
mv -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/vega*.xn* @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/

#rm -f @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/Backup/*vega.fa
#mv -f @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_vega/*.fa  @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/Backup/

# not sure we need the fasta files, just need the .xn files


cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/vega_zfin.xnd @BLASTSERVER_BLAST_DATABASE_PATH@/Current
cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/vega_zfin.xni @BLASTSERVER_BLAST_DATABASE_PATH@/Current
cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/vega_zfin.xns @BLASTSERVER_BLAST_DATABASE_PATH@/Current
cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/vega_zfin.xnt @BLASTSERVER_BLAST_DATABASE_PATH@/Current


cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/vega_transcript.xnd @BLASTSERVER_BLAST_DATABASE_PATH@/Current
cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/vega_transcript.xni @BLASTSERVER_BLAST_DATABASE_PATH@/Current
cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/vega_transcript.xns @BLASTSERVER_BLAST_DATABASE_PATH@/Current
cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/vega_transcript.xnt @BLASTSERVER_BLAST_DATABASE_PATH@/Current

#cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/vega_trans.xnd @BLASTSERVER_BLAST_DATABASE_PATH@/Current
#cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/vega_trans.xni @BLASTSERVER_BLAST_DATABASE_PATH@/Current
#cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/vega_trans.xns @BLASTSERVER_BLAST_DATABASE_PATH@/Current
#cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/vega_trans.xnt @BLASTSERVER_BLAST_DATABASE_PATH@/Current

cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/vega_withdrawn.xnd @BLASTSERVER_BLAST_DATABASE_PATH@/Current
cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/vega_withdrawn.xni @BLASTSERVER_BLAST_DATABASE_PATH@/Current
cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/vega_withdrawn.xns @BLASTSERVER_BLAST_DATABASE_PATH@/Current
cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/vega_withdrawn.xnt @BLASTSERVER_BLAST_DATABASE_PATH@/Current

# @TARGET_PATH@/ZFIN/zfin_vega/distributeToNodeszfin_vega.sh

chmod g+w @BLASTSERVER_BLAST_DATABASE_PATH@/Current/*

echo "done cpvega_zfin.sh" 

exit

