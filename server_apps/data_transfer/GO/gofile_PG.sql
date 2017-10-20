begin work ;
		 
create temporary table tmp_identifiers (id text, id2 text);
copy tmp_identifiers from '<!--|ROOT_PATH|-->/server_apps/data_transfer/GO/ids.unl' (delimiter '|');

create index tmpidentifiers_index on tmp_identifiers (id);


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
			 gene_type text);

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
       gene_type
)
select mrkrgoev_zdb_id,
				mrkr_zdb_id, mrkr_abbrev, mrkr_name, term_ont_id, mrkrgoev_source_zdb_id,
				accession_no, mrkrgoev_evidence_code, infgrmem_inferred_from, mrkrgoev_gflag_name,
				upper(substring(term_ontology from 1 for 1)), mrkrgoev_date_modified, mrkrgoev_annotation_organization_created_by, lower(szm_term_name)
			   from marker_go_term_evidence
			   join marker on mrkrgoev_mrkr_zdb_id = mrkr_zdb_id
			   join term on mrkrgoev_term_zdb_id = term_zdb_id
			   join publication on mrkrgoev_source_zdb_id  = zdb_id
			   join so_zfin_mapping on mrkr_type = szm_object_type
		           full outer join inference_group_member on mrkrgoev_zdb_id = infgrmem_mrkrgoev_zdb_id;

select distinct gene_type from tmp_go where gene_type is not null;

update tmp_go
  set id2 = (select id2 from tmp_identifiers
      	    	    where m_zdb_id = id); 
  

\copy (select * from tmp_go) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/GO/go.zfin' with delimiter as '	' null as '';

commit work;
