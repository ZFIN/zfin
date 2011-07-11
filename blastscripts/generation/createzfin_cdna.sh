#!/bin/bash

# dump accession files
echo "unload to \"zfin_genbank_cdna_acc.unl\" delimiter \" \" select dblink_acc_num from db_link where dblink_fdbcont_zdb_id in (select fdbcont_zdb_id from foreign_db_contains, foreign_db, foreign_db_data_type where fdb_db_name = \"GenBank\" and fdbdt_super_type = \"sequence\" and fdbcont_fdbdt_id = fdbdt_pk_id and fdbcont_fdb_db_id = fdb_db_pk_id" | dbaccess $DBNAME ;

# dump accession file from zfin_all
$BLAST_BINARY/xdget  -n -f -e xdget_zfin_cdna.log -o $BLAST_PATH/zfin_cdna.fa -Tgb1 $BLAST_PATH/gbk_zf_all zfin_genbank_cdna_acc.unl ;


# append vega_zfin
#dumpvega_zfin_tofasta.sh ; # TODO# I don't think we do this, but not sure
#cat $BLAST_PATH/vega_zfin.fa >> $BLAST_PATH/zfin_cdna.fa  ; 



# create new blast database
$BLAST_BINARY/xdformat -n -o $BLAST_PATH/zfin_cdna -e xdformat_zfin_cdna.log -I -Tgb1 -Ttpe -t "ZFIN cDNA Sequence Set" $BLAST_PATH/zfin_cdna.fa ;



