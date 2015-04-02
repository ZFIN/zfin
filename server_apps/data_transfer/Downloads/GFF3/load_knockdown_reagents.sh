#!/bin/sh

GLOBALSTORE="/research/zprodmore/gff3"
TARGETDIR="$TARGETROOT/server_apps/data_transfer/Downloads/GFF3"

BOWTIE_IDX="$GLOBALSTORE/Ensembl_Zv9.78"

cd $TARGETDIR
rm -f mo_seq.fa_line mo_seq.fa E_mo_seq.sam E_zfin_morpholino.gff3 mo_seq_E_miss.fa talen_seq_1.fa_line \
    talen_seq_2.fa_line talen_seq_1.fa talen_seq_2.fa E_talen_seq.sam E_zfin_talen.gff3 talen_seq_E_miss.fa \
    E_zfin_knockdown_reagents.gff3 E_zfin_knockdown_reagents.unl mapped_strs.unl E_talen_seq_merged.sam \
    E_mo_seq_merged.sam

dbaccess -a $DBNAME << SQL
    unload to mapped_strs.unl
    select mrel_mrkr_1_zdb_id, sfcl_chromosome from sequence_feature_chromosome_location
    inner join marker_relationship on mrel_mrkr_2_zdb_id = sfcl_data_zdb_id
    where sfcl_location_source = 'ZfinGbrowseStartEndLoader'
    and mrel_type = 'knockdown reagent targets gene'
SQL

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# BOWTIE v1 ALIGNMENT FOR MOs AND CRISPRs

dbaccess -a $DBNAME get_mo_and_crispr_seq.sql
tr \~ '\n' < mo_seq.fa_line > mo_seq.fa
/opt/misc/bowtie/bowtie --all --best --strata --sam -f $BOWTIE_IDX mo_seq.fa > E_mo_seq.sam
./merge_morpholino.groovy mapped_strs.unl E_mo_seq.sam > E_mo_seq_merged.sam
./sam2gff3.groovy < E_mo_seq_merged.sam > E_zfin_morpholino.gff3 2> mo_seq_E_miss.fa


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# BOWTIE2 TALEN ALIGNMENT

dbaccess -a $DBNAME get_talen_seq_1.sql get_talen_seq_2.sql
tr \~ '\n' < talen_seq_1.fa_line > talen_seq_1.fa
tr \~ '\n' < talen_seq_2.fa_line > talen_seq_2.fa
/opt/misc/bowtie2/bowtie2 -x $BOWTIE_IDX -f -1 talen_seq_1.fa -2 talen_seq_2.fa -S E_talen_seq.sam
./merge_morpholino.groovy mapped_strs.unl E_talen_seq.sam > E_talen_seq_merged.sam
./sam2gff3.groovy --useZdbId < E_talen_seq_merged.sam > E_zfin_talen.gff3 2> talen_seq_E_miss.fa


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# COMBINE MO/CRISPR AND TALEN GFF3 FILES

./merge_gff3.groovy E_zfin_knockdown_reagents.gff3 E_zfin_morpholino.gff3 E_zfin_talen.gff3
./gff32unl.groovy E_zfin_knockdown_reagents.gff3 > E_zfin_knockdown_reagents.unl
cat load_knockdown_reagents.sql commit.sql | dbaccess -a $DBNAME
