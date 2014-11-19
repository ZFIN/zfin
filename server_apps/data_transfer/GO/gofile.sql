begin work ;

 select dalias_data_Zdb_id as id1, dalias_alias as id2
 from marker, data_alias
 where mrkr_zdb_id = dalias_data_zdb_id
into temp tmp_3 with no log;
		 
create temp table tmp_identifiers (id varchar(50), id2 lvarchar(1500))
with no log;

insert into tmp_identifiers (id)
 select distinct id1 from tmp_3;

create index tmp3_index on tmp_3 (id1)
using btree in idxdbs3;

create index tmpidentifiers_index on tmp_identifiers (id)
using btree in idxdbs2;

update tmp_identifiers
  set id2 = replace(replace(replace(substr(multiset (select distinct item replace(id2,",","Sierra") from tmp_3
							  where tmp_3.id1 = tmp_identifiers.id

							 )::lvarchar(4000),11),""),"'}",""),"'","");

select first 1  mrkrgoev_zdb_id,
				mrkr_zdb_id, mrkr_abbrev, mrkr_name, term_ont_id, mrkrgoev_source_zdb_id,
				accession_no, mrkrgoev_evidence_code, infgrmem_inferred_from, mrkrgoev_gflag_name,
				upper(term_ontology[1]), mrkrgoev_date_modified, mrkrgoev_annotation_organization_created_by, id2
			   from marker_go_term_evidence, marker, term, publication, tmp_identifiers,
					   outer inference_group_member
			  where mrkrgoev_mrkr_zdb_id = mrkr_zdb_id
			    and mrkrgoev_term_zdb_id = term_zdb_id
			    and mrkrgoev_source_zdb_id  = zdb_id
			    and mrkrgoev_zdb_id = infgrmem_mrkrgoev_zdb_id 
			    and id = mrkr_zdb_id
 ;

unload to 'go.zfin' delimiter '	' 
			 select mrkrgoev_zdb_id,
				mrkr_zdb_id, mrkr_abbrev, mrkr_name, term_ont_id, mrkrgoev_source_zdb_id,
				accession_no, mrkrgoev_evidence_code, infgrmem_inferred_from, mrkrgoev_gflag_name,
				upper(term_ontology[1]), mrkrgoev_date_modified, mrkrgoev_annotation_organization_created_by, id2
			   from marker_go_term_evidence, marker, term, publication, tmp_identifiers,
					   outer inference_group_member
			  where mrkrgoev_mrkr_zdb_id = mrkr_zdb_id
			    and mrkrgoev_term_zdb_id = term_zdb_id
			    and mrkrgoev_source_zdb_id  = zdb_id
			    and mrkrgoev_zdb_id = infgrmem_mrkrgoev_zdb_id 
			    and id = mrkr_zdb_id;

commit work;