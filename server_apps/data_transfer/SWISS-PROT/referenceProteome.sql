begin work;

create temp table refProteome (
    uniprot_id         varchar(50)    
) with no log;

load from refProteome.tab insert into refProteome;

unload to '/opt/zfin/www_homes/manx/server_apps/data_transfer/SWISS-PROT/refProteomeSorted.tab'
 DELIMITER "	"
  select distinct uniprot_id
    from refProteome
   where uniprot_id is not null
 order by uniprot_id;

unload to '/opt/zfin/www_homes/manx/server_apps/data_transfer/SWISS-PROT/genesWithProteinAndWithXpatOrPhenoNotWithRefPr.tab'
 DELIMITER "	"
  select distinct mrkr_abbrev, mrkr_zdb_id
    from marker 
   where mrkr_type = 'GENE'
     and exists(select 'x' from db_link, foreign_db_contains, foreign_db
                 where fdbcont_fdbdt_id = 2
                   and fdbcont_organism_common_name = 'Zebrafish'
                   and mrkr_zdb_id = dblink_linked_recid
                   and dblink_fdbcont_zdb_id = fdbcont_zdb_id
                   and fdb_db_pk_id = fdbcont_fdb_db_id)
                   and (exists(select 'x' from expression_experiment where xpatex_gene_zdb_id = mrkr_zdb_id)
                     or exists(select 'x' from mutant_fast_Search where mfs_mrkr_zdb_id = mrkr_zdb_id))
     and not exists(select 'x' from db_link, refProteome
                     where mrkr_zdb_id = dblink_linked_recid
                       and dblink_acc_num = uniprot_id)
   order by mrkr_abbrev;

commit work;
