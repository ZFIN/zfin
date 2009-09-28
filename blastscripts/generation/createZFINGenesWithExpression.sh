#!/bin/bash
# based on zfin_gene_xpat_cdna_acc
# from genomix:weeklyZfinSeq_cDNASeq.sh 
# accessions from server_apps/data_transfer/BLAST/getZfinGbAcc.pl


## query genbank RNA and vega transcripts accessions on genes 
## that has expression data, and not named microRNA%
#my $sql_xpat ="
#
#create temp table tmp_xpatmrkr_zdb_id_list (t_xgl_mrkr_zdb_id	varchar(50) )with no log;
#
#insert into tmp_xpatmrkr_zdb_id_list 
#     select distinct xpatex_gene_zdb_id 
#       from expression_experiment, marker 
#      where xpatex_gene_zdb_id =  mrkr_zdb_id
#        and mrkr_name[1,8] <> \"microRNA\"
#        and exists (select xpatres_zdb_id from expression_result where xpatres_xpatex_zdb_id = xpatex_zdb_id);
#
#insert into tmp_xpatmrkr_zdb_id_list 
#     select distinct mrel_mrkr_2_zdb_id 
#       from tmp_xpatmrkr_zdb_id_list, marker_relationship 
#      where t_xgl_mrkr_zdb_id = mrel_mrkr_1_zdb_id 
#        and mrel_type = \"gene encodes small segment\";
#
#unload to \"$accFile_xpat\" delimiter \" \" 
#   select distinct dblink_acc_num 
#     from db_link, foreign_db_contains, foreign_db, foreign_db_data_type,tmp_xpatmrkr_zdb_id_list
#    where dblink_linked_recid = t_xgl_mrkr_zdb_id
#      and dblink_fdbcont_zdb_id = fdbcont_zdb_id 
#      and fdb_db_name in (\"GenBank\",\"Vega_Trans\") 
#      and fdbdt_data_type = \"RNA\"
#      and fdbcont_fdbdt_id = fdbdt_pk_id
#      and fdbcont_fdb_db_id = fdb_db_pk_id
#";  






