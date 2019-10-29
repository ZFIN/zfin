begin work ;
		 
create temporary table tmp_identifiers (id text, id2 text);
\copy tmp_identifiers from '<!--|ROOT_PATH|-->/server_apps/data_transfer/GO/ids.unl' (delimiter '|');

create index tmpidentifiers_index on tmp_identifiers (id);

create temporary table tmp_go_annot (mev_zdb_id text,anno_group_id bigint,annoextn text);


insert into tmp_go_annot (mev_zdb_id,anno_group_id,annoextn)
select distinct mrkrgoev_zdb_id, 
                mgtaeg_annotation_extension_group_id, 
                term_name||'('||mgtae_term_text||')'
  from marker_go_term_evidence, 
       marker_go_term_annotation_extension_group, 
       marker_go_term_annotation_extension,
       term
  where mrkrgoev_zdb_id= mgtaeg_mrkrgoev_zdb_id 
      and mgtae_relationship_term_zdb_id=term_zdb_id 
      and mgtae_extension_group_id=mgtaeg_annotation_extension_group_id ;

create temporary table tmp_go_identifiers (goid text, goid2 bigint,goid3 text);

insert into tmp_go_identifiers(goid, goid2, goid3)
select mev_zdb_id,anno_group_id, STRING_AGG(annoextn,',')
from tmp_go_annot
group by mev_zdb_id,anno_group_id
order by 1;

create temporary table tmp_go_identifiers_pipes (goidtmp text, goid3tmp text);
insert into tmp_go_identifiers_pipes (goidtmp)
 select distinct goid from tmp_go_identifiers;

update tmp_go_identifiers_pipes

  set goid3tmp=(select STRING_AGG(distinct goid3,'|') 
                  from tmp_go_identifiers 
                  where tmp_go_identifiers.goid = tmp_go_identifiers_pipes.goidtmp);
set goid3tmp=(select STRING_AGG(goid3,'|') from tmp_go_identifiers where tmp_go_identifiers.goid = tmp_go_identifiers_pipes.goidtmp and godid3 not like '%GO%');
update tmp_go_identifiers_pipes



set goid3tmp=(select STRING_AGG(goid3,'|') from tmp_go_identifiers where tmp_go_identifiers.goid = tmp_go_identifiers_pipes.goidtmp and godid3 not like '%GO%');
update tmp_go_identifiers_pipes
set goid3tmp=(select goid3 from tmp_go_identifiers where tmp_go_identifiers.goid = tmp_go_identifiers_pipes.goidtmp and godid3 like '%GO%');


create temporary table tmp_go_proteinid (mgev_zdb_id text,proteinid text);

insert into tmp_go_proteinid (mgev_zdb_id,proteinid) select distinct mrkrgoev_zdb_id, nvl(fdb_db_name||':'||dblink_acc_num,'')
from marker_go_term_evidence, foreign_db, db_link,foreign_db_contains
where mrkrgoev_protein_dblink_zdb_id= dblink_Zdb_id and dblink_fdbcont_zdb_id=fdbcont_zdb_id and fdbcont_fdb_db_id=fdb_db_pk_id ;


create temporary table tmp_go (mv_zdb_id text,
       	    	  	 m_zdb_id text,
			 m_abbrev text,
			 m_name text,
			 t_ont_id text,
			 mv_source_id text,
			 ac_no text,
			 mv_ev_code text,
			 if_from text,
			 mv_flag text,
			 t_ont text,
			 mv_date_modified timestamp without time zone,
			 mv_created_by text,
			 id2 text,
			 mv_annoextn text,
			 gene_type text,geneproduct_id text,doi_id text,goref_id text);


insert into tmp_go (mv_zdb_id,
       m_zdb_id,
       m_abbrev,
       m_name, 
       t_ont_id,
       mv_source_id,
       ac_no,
       mv_ev_code,
       if_from,
       mv_flag,
       t_ont,
       mv_date_modified,
       mv_created_by,
       mv_annoextn,
       gene_type,
       geneproduct_id,doi_id,goref_id
)
select mrkrgoev_zdb_id,
				mrkr_zdb_id, mrkr_abbrev, mrkr_name, term_ont_id, mrkrgoev_source_zdb_id,
				accession_no, mrkrgoev_evidence_code, infgrmem_inferred_from, mrkrgoev_gflag_name,
				upper(substring(term_ontology from 1 for 1)), mrkrgoev_date_modified, 
                                mrkrgoev_annotation_organization_created_by,goid3tmp,lower(szm_term_name),proteinid,pub_doi,pub_goref_id
			   from marker_go_term_evidence
			   join marker on mrkrgoev_mrkr_zdb_id = mrkr_zdb_id
			   join term on mrkrgoev_term_zdb_id = term_zdb_id
			   join publication on mrkrgoev_source_zdb_id  = zdb_id
			   join so_zfin_mapping on mrkr_type = szm_object_type
		           full outer join inference_group_member on mrkrgoev_zdb_id = infgrmem_mrkrgoev_zdb_id
		           full outer join tmp_go_identifiers_pipes on  mrkrgoev_zdb_id=goidtmp
			         full outer join tmp_go_proteinid on mrkrgoev_zdb_id=mgev_zdb_id ;

select distinct gene_type from tmp_go where gene_type is not null;

update tmp_go
  set id2 = (select id2 from tmp_identifiers
      	    	    where m_zdb_id = id); 
update tmp_go
set mv_created_by='UniProt' where mv_created_by='UniProtKB';

\copy (select * from tmp_go) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/GO/go.zfin' with delimiter as '	' null as '';

commit work;
