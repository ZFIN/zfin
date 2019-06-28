--liquibase formatted sql
--changeset xshao:CUR-912

update construct 
   set construct_name = 'Tg(FRT-Xla.Actc1:DsRed-GAB-FRT,LOXP-Hsa.rs2275035_C-LOXP-gata2a:EGFP-5HS4)' 
 where construct_zdb_id='ZDB-TGCONSTRCT-190619-2';
 
update marker 
   set mrkr_name = 'Tg(FRT-Xla.Actc1:DsRed-GAB-FRT,LOXP-Hsa.rs2275035_C-LOXP-gata2a:EGFP-5HS4)',
       mrkr_abbrev = 'Tg(FRT-Xla.Actc1:DsRed-GAB-FRT,LOXP-Hsa.rs2275035_C-LOXP-gata2a:EGFP-5HS4)'
 where mrkr_zdb_id = 'ZDB-TGCONSTRCT-190619-2';
 
update construct_component
   set cc_order = 26
 where cc_pk_id = 53138;
 
update construct_component
   set cc_order = 25
 where cc_pk_id = 53137;
 
update construct_component
   set cc_order = 24
 where cc_pk_id = 53136;
 
update construct_component
   set cc_order = 23
 where cc_pk_id = 53135;  
 
update construct_component
   set cc_order = 22
 where cc_pk_id = 53134;
 
update construct_component
   set cc_order = 21
 where cc_pk_id = 53133;
 
update construct_component
   set cc_order = 20
 where cc_pk_id = 53132;
 
update construct_component
   set cc_order = 19
 where cc_pk_id = 53131;  
 
update construct_component
   set cc_order = 18
 where cc_pk_id = 53130;
 
update construct_component
   set cc_order = 17
 where cc_pk_id = 53129;  
 
update construct_component
   set cc_order = 16
 where cc_pk_id = 53128;
 
update construct_component
   set cc_order = 15
 where cc_pk_id = 53127;
 
update construct_component
   set cc_order = 14
 where cc_pk_id = 53126;
 
update construct_component
   set cc_order = 13
 where cc_pk_id = 53125;  
 
update construct_component
   set cc_order = 12
 where cc_pk_id = 53124;
 
update construct_component
   set cc_order = 11
 where cc_pk_id = 53123;
 
update construct_component
   set cc_order = 10
 where cc_pk_id = 53122;  
 
insert into construct_component (cc_construct_zdb_id,cc_component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order)
 values ('ZDB-TGCONSTRCT-190619-2','coding sequence of','coding component','ZDB-EFG-070925-1','DsRed',1,8);
 
insert into construct_component (cc_construct_zdb_id,cc_component_type,cc_component_category,cc_component,cc_cassette_number,cc_order)
 values ('ZDB-TGCONSTRCT-190619-2','coding component','coding component','-',1,9); 
 
create temp table tmp_cmrel(relid text, consid text,mrkrid text,reltype text);
insert into tmp_cmrel(consid, mrkrid, reltype) 
values ('ZDB-TGCONSTRCT-190619-2','ZDB-EFG-070925-1','coding sequence of');

update tmp_cmrel 
   set relid = get_id('CMREL');
   
insert into zdb_active_data(zactvd_zdb_id) 
select relid from tmp_cmrel;

insert into construct_marker_relationship(conmrkrrel_zdb_id, conmrkrrel_construct_zdb_id, conmrkrrel_mrkr_zdb_id, conmrkrrel_relationship_type) 
select relid,consid, mrkrid, reltype 
  from tmp_cmrel;

drop table tmp_cmrel;

