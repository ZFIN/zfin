begin work;

create temp table refProteome (
    uniprot_id         text    
) ;

copy refProteome from '<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/refProteome.tab' (delimiter '	');

create view refProtSorted as
  select distinct uniprot_id
    from refProteome
   where uniprot_id is not null
 order by uniprot_id;
\copy (select * from refProtSorted) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/refProteomeSorted.tab' with delimiter as '	' null as '';
drop view refProtSorted;

create view genesWithProteinAndWithXpatOrPhenoNotWithRefPr as
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
\copy (select * from genesWithProteinAndWithXpatOrPhenoNotWithRefPr) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/genesWithProteinAndWithXpatOrPhenoNotWithRefPr.tab' with delimiter as '	' null as '';
drop view genesWithProteinAndWithXpatOrPhenoNotWithRefPr;

commit work;
