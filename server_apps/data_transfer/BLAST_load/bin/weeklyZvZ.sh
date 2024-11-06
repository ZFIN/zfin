#!/bin/tcsh

# blastn zfin_nt v.s. zfin_nt sequences
# 
# It is launched by cron job each Sunday morning
# starting at 1am. The input batch sequence file
# is splitted into 9 subfiles and sent to 9 (out 
# of the 12) job slots on the cluster.
# 

setenv BLASTDB /private/blastdb/wu-db
setenv SGE_ROOT /common/sge
set BLASTSERVER_FASTA_FILE_PATH="../staging/ZFIN/zfin_cdna/"

nice +10 \
/common/bin/mblasta -p blastn \
                    -d zfin_cdna_seq \
                    -i @BLASTSERVER_FASTA_FILE_PATH@/zfin_cdna_seq.fa \
                    -e e-100 \
                    -dbsplit 1 \
		    -isplit  9 \
                    -o zvz_e100.out;
