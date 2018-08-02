begin work ;

create temporary table geneNameUpdate (
    gene_zdb_id   text not null,
    existing_name text not null,
    updated_name  text  not null,
    gene_symbol  text not null
);

create index geneNameUpdate_gene_zdb_id_idx on geneNameUpdate(gene_zdb_id) ;
create index geneNameUpdate_existing_name_idx on geneNameUpdate(existing_name) ;
create index geneNameUpdate_updated_name_idx on geneNameUpdate(updated_name) ;
create index geneNameUpdate_gene_symbol_idx on geneNameUpdate(gene_symbol) ;

--!echo 'Load from namesToUpdate.unl'
\copy geneNameUpdate from '<!--|ROOT_PATH|-->/server_apps/data_transfer/ORTHO/namesToUpdate.unl' (delimiter '|');

alter table geneNameUpdate add nomen_zdb_id text;

update geneNameUpdate set nomen_zdb_id = get_id('NOMEN');
   
--!echo 'update gene names'

update marker set mrkr_name = (
 select updated_name
   from geneNameUpdate
  where mrkr_zdb_id = gene_zdb_id
) where mrkr_type = 'GENE'
    and exists (select 'x' from geneNameUpdate
                 where gene_zdb_id = mrkr_zdb_id);      
                     

--!echo 'insert updates table'
insert into updates (rec_id,field_name,new_value,old_value,upd_when,comments) 
select gene_zdb_id,'mrkr_name',updated_name,existing_name,now(),'gene renamed by Ken and the orthology script according to NCBI orthology data' as text2
  from geneNameUpdate;

insert into zdb_active_data(zactvd_zdb_id)
select nomen_zdb_id
  from geneNameUpdate;

--!echo 'update marker_history table'
insert into marker_history (mhist_zdb_id,mhist_mrkr_zdb_id,mhist_event,mhist_reason,mhist_date,mhist_mrkr_name_on_mhist_date,mhist_mrkr_abbrev_on_mhist_date,mhist_mrkr_prev_name,mhist_comments) 
select nomen_zdb_id,gene_zdb_id,'renamed','renamed to conform with zebrafish guidelines' as text2,now(),updated_name,gene_symbol,existing_name,'Renamed as part of ZFIN nomenclature update project'  as text3
  from geneNameUpdate;
  
\copy (select * from geneNameUpdate) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/ORTHO/geneNamesUpdatedReport' with delimiter as '|' null as '';

commit work;

--rollback work;
