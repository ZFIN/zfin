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

create temp table tmp_go (mv_zdb_id varchar(50),
       	    	  	 m_zdb_id varchar(50),
			 m_abbrev lvarchar,
			 m_name lvarchar,
			 t_ont_id varchar(50),
			 mv_source_id varchar(50),
			 ac_no int8,
			 mv_ev_code varchar(30),
			 if_from lvarchar,
			 mv_flag varchar(50),
			 t_ont varchar(100),
			 mv_date_modified datetime year to second,
			 mv_created_by varchar(100),
			 id2 lvarchar(4000))
with no log;

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
       mv_created_by
)
select mrkrgoev_zdb_id,
				mrkr_zdb_id, mrkr_abbrev, mrkr_name, term_ont_id, mrkrgoev_source_zdb_id,
				accession_no, mrkrgoev_evidence_code, infgrmem_inferred_from, mrkrgoev_gflag_name,
				upper(term_ontology[1]), mrkrgoev_date_modified, mrkrgoev_annotation_organization_created_by
			   from marker_go_term_evidence, marker, term, publication,
					   outer inference_group_member
			  where mrkrgoev_mrkr_zdb_id = mrkr_zdb_id
			    and mrkrgoev_term_zdb_id = term_zdb_id
			    and mrkrgoev_source_zdb_id  = zdb_id
			    and mrkrgoev_zdb_id = infgrmem_mrkrgoev_zdb_id ;

update tmp_go
  set id2 = (select id2 from tmp_identifiers
      	    	    where m_zdb_id = id); 

select first 1 * from tmp_go
 where m_abbrev = 'pax8';

select first 1 * from tmp_go;
  

unload to 'go.zfin' delimiter '	' 
		select * from tmp_go;

commit work;