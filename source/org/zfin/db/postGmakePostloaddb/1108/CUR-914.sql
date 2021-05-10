--liquibase formatted sql
--changeset xshao:CUR-914

create temp table tmp_dalias_cur914 (aliasid text, constructid text, constructname text);

insert into tmp_dalias_cur914 (aliasid, constructid ,constructname) 
select get_id('DALIAS'), construct_zdb_id, construct_name  
  from construct 
 where construct_zdb_id = 'ZDB-TGCONSTRCT-120103-2';

insert into zdb_active_data (zactvd_zdb_id) 
select aliasid from tmp_dalias_cur914;

insert into data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_group_id) 
select aliasid, constructid, constructname, 1 
  from tmp_dalias_cur914;
  
  
update construct 
   set construct_name = 'Tg(mbp:EGFP,myl7:EGFP)' 
 where construct_zdb_id = 'ZDB-TGCONSTRCT-190619-2';
 
update marker 
   set mrkr_name = 'Tg(mbp:EGFP,myl7:EGFP)',
       mrkr_abbrev = 'Tg(mbp:EGFP,myl7:EGFP)'
 where mrkr_zdb_id = 'construct_zdb_id';
 
update construct_component
   set cc_order = 2
 where cc_pk_id = 11260;
 
update construct_component
   set cc_order = 3
 where cc_pk_id = 11264;  
 
update construct_component
   set cc_order = 4
 where cc_pk_id = 11259;
 
update construct_component
   set cc_order = 5
 where cc_pk_id = 11258;
 
update construct_component
   set cc_order = 10
 where cc_pk_id = 11261;  
 
insert into construct_component (cc_construct_zdb_id,cc_component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order)
 values ('ZDB-TGCONSTRCT-120103-2','controlled vocab component','promoter component','ZDB-CV-150506-11',',',2,6);
 
insert into construct_component (cc_construct_zdb_id,cc_component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order)
 values ('ZDB-TGCONSTRCT-120103-2','text component','promoter component','ZDB-GENE-991019-3','myl7',2,7);

insert into construct_component (cc_construct_zdb_id,cc_component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order)
 values ('ZDB-TGCONSTRCT-120103-2','controlled vocab component','promoter component','ZDB-CV-150506-10',':',2,8);
 
insert into construct_component (cc_construct_zdb_id,cc_component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order)
 values ('ZDB-TGCONSTRCT-120103-2','coding sequence of','coding sequence component','ZDB-EFG-070117-1','EGFP',2,9);
 
create temp table tmp_cmrel_cur914(relid text, consid text,mrkrid text,reltype text);
insert into tmp_cmrel_cur914(consid, mrkrid, reltype) 
values ('ZDB-TGCONSTRCT-120103-2','ZDB-GENE-991019-3','promoter of');

update tmp_cmrel_cur914 
   set relid = get_id('CMREL');
   
insert into zdb_active_data(zactvd_zdb_id) 
select relid from tmp_cmrel_cur914;

insert into construct_marker_relationship(conmrkrrel_zdb_id, conmrkrrel_construct_zdb_id, conmrkrrel_mrkr_zdb_id, conmrkrrel_relationship_type) 
select relid, consid, mrkrid, reltype 
  from tmp_cmrel_cur914;

