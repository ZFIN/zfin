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

create temp table tmp_go_annot (mev_zdb_id varchar(50),anno_group_id int8,annoextn lvarchar(255)) with no log;
insert into tmp_go_annot (mev_zdb_id,anno_group_id,annoextn) select distinct mrkrgoev_zdb_id, mgtaeg_annotation_extension_group_id, term_name||'('||mgtae_term_text||')'
from marker_go_term_evidence, marker_go_term_annotation_extension_group, marker_go_term_annotation_extension,term
where mrkrgoev_zdb_id= mgtaeg_mrkrgoev_zdb_id and mgtae_relationship_term_zdb_id=term_zdb_id and mgtae_extension_group_id=mgtaeg_annotation_extension_group_id ;

create temp table tmp_go_identifiers (goid varchar(50), goid2 int8,goid3 lvarchar(1500))
with no log;

insert into tmp_go_identifiers (goid,goid2)
 select distinct mev_zdb_id,anno_group_id from tmp_go_annot;

update tmp_go_identifiers  set goid3 = replace(replace(replace(substr(multiset (select distinct item replace(annoextn,",","Prita") from tmp_go_annot
                                                          where tmp_go_annot.mev_zdb_id = tmp_go_identifiers.goid and tmp_go_annot.anno_group_id=tmp_go_identifiers.goid2

                                                         )::lvarchar(4000),11),""),"'}",""),"'","");

create temp table tmp_go_identifiers_pipes (goidtmp varchar(50), goid3tmp lvarchar(2000))
with no log;

insert into tmp_go_identifiers_pipes (goidtmp)
 select distinct goid from tmp_go_identifiers;

update tmp_go_identifiers_pipes  set goid3tmp = replace(replace(replace(substr(multiset (select distinct item replace(goid3,",","Prita") from tmp_go_identifiers
                                                          where tmp_go_identifiers.goid = tmp_go_identifiers_pipes.goidtmp
                                                          )::lvarchar(4000),11),""),"'}",""),"'","");



unload to 'tmp_go_idspipes' select * from tmp_go_identifiers_pipes order by goidtmp;

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
			 id2 lvarchar(4000),
			 mv_annoextn lvarchar(1000),
			 gene_type varchar(100))
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
       mv_created_by,
       mv_annoextn,
       gene_type
)
select mrkrgoev_zdb_id,
				mrkr_zdb_id, mrkr_abbrev, mrkr_name, term1.term_ont_id, mrkrgoev_source_zdb_id,
				accession_no, mrkrgoev_evidence_code, infgrmem_inferred_from, mrkrgoev_gflag_name,
				upper(term_ontology[1]), mrkrgoev_date_modified, mrkrgoev_annotation_organization_created_by,goid3tmp,lower(szm_term_name)
			   from marker_go_term_evidence, marker, term term1, publication, so_zfin_mapping,
					   outer inference_group_member,
					   outer  tmp_go_identifiers_pipes
			  where mrkrgoev_mrkr_zdb_id = mrkr_zdb_id
			    and mrkrgoev_term_zdb_id = term1.term_zdb_id
			    and mrkrgoev_source_zdb_id  = zdb_id
			    and mrkr_type = szm_object_type
			    and mrkrgoev_zdb_id = infgrmem_mrkrgoev_zdb_id
			    and mrkrgoev_zdb_id=goidtmp;


select distinct gene_type from tmp_go where gene_type is not null;

update tmp_go
  set id2 = (select id2 from tmp_identifiers
      	    	    where m_zdb_id = id); 
update tmp_go
set mv_created_by='UniProt' where mv_created_by='UniProtKB';
select * from tmp_go where mv_zdb_id='ZDB-MRKRGOEV-180328-54';
select first 1 * from tmp_go
 where m_abbrev = 'pax8';

select first 1 * from tmp_go;
  

unload to 'go.zfin' delimiter '	' 
		select * from tmp_go;

commit work;
