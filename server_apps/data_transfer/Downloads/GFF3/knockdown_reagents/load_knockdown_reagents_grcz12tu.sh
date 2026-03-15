#!/bin/bash -e

GLOBALSTORE="/research/zprodmore/gff3"
TARGETDIR="$TARGETROOT/server_apps/data_transfer/Downloads/GFF3/knockdown_reagents/"

BOWTIE_IDX="$GLOBALSTORE/GRCz12tu/GRCz12tu"

cd $TARGETDIR || exit
rm -f mo_seq_grcz12tu.fa_line mo_seq_grcz12tu.fa E_mo_seq_grcz12tu.sam E_zfin_morpholino_grcz12tu.gff3 mo_seq_grcz12tu_miss.fa \
    talen_seq_1_grcz12tu.fa_line talen_seq_2_grcz12tu.fa_line talen_seq_1_grcz12tu.fa talen_seq_2_grcz12tu.fa \
    E_talen_seq_grcz12tu.sam E_zfin_talen_grcz12tu.gff3 talen_seq_grcz12tu_miss.fa \
    E_zfin_knockdown_reagents_grcz12tu.gff3 E_zfin_knockdown_reagents_grcz12tu.unl

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# BOWTIE v1 ALIGNMENT FOR MOs AND CRISPRs

${PGBINDIR}/psql -v ON_ERROR_STOP=1 $DBNAME < get_mo_and_crispr_seq.sql
tr \~ '\n' < mo_seq.fa_line > mo_seq_grcz12tu.fa
/opt/misc/bowtie/bowtie --all --best --strata --sam -f $BOWTIE_IDX mo_seq_grcz12tu.fa > E_mo_seq_grcz12tu.sam
./sam2gff3.groovy --source ZFIN_knockdown_reagent_GRCz12tu < E_mo_seq_grcz12tu.sam > E_zfin_morpholino_grcz12tu.gff3 2> mo_seq_grcz12tu_miss.fa


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# BOWTIE2 TALEN ALIGNMENT

${PGBINDIR}/psql -v ON_ERROR_STOP=1 -d $DBNAME -f get_talen_seq_1.sql -f get_talen_seq_2.sql
tr \~ '\n' < talen_seq_1.fa_line > talen_seq_1_grcz12tu.fa
tr \~ '\n' < talen_seq_2.fa_line > talen_seq_2_grcz12tu.fa
/opt/misc/bowtie2/bowtie2 -x $BOWTIE_IDX  --no-discordant --no-mixed -X 750 -f -1 talen_seq_1_grcz12tu.fa -2 talen_seq_2_grcz12tu.fa -S E_talen_seq_grcz12tu.sam
./sam2gff3.groovy --useZdbId --source ZFIN_knockdown_reagent_GRCz12tu < E_talen_seq_grcz12tu.sam > E_zfin_talen_grcz12tu.gff3 2> talen_seq_grcz12tu_miss.fa


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# COMBINE MO/CRISPR AND TALEN GFF3 FILES

./merge_gff3.groovy E_zfin_knockdown_reagents_grcz12tu.gff3 E_zfin_morpholino_grcz12tu.gff3 E_zfin_talen_grcz12tu.gff3
./gff32unl.groovy E_zfin_knockdown_reagents_grcz12tu.gff3 > E_zfin_knockdown_reagents_grcz12tu.unl
${PGBINDIR}/psql -v ON_ERROR_STOP=1 -d $DBNAME -f load_knockdown_reagents_grcz12tu.sql

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# update sequence_feature_chromosome_location_generated table
psql -v ON_ERROR_STOP=1 -d $DB_NAME -a -f ../../../Ensembl/updateSequenceFeatureChromosomeLocation.sql
