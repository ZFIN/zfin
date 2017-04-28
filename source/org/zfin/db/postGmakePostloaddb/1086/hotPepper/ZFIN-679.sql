--liquibase formatted sql
--changeset prita:ZFIN-679

update construct set construct_name='TgPAC(myb:2xmyb-EGFP)' where construct_zdb_id='ZDB-TGCONSTRCT-071017-1';
update marker set mrkr_name='TgPAC(myb:2xmyb-EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-071017-1';
update marker set mrkr_abbrev='TgPAC(myb:2xmyb-EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-071017-1';


delete from construct_component where cc_construct_Zdb_id='ZDB-TGCONSTRCT-071017-1' and cc_order=2;
delete from construct_component where cc_construct_Zdb_id='ZDB-TGCONSTRCT-071017-1' and cc_order=3;
delete from construct_component where cc_construct_Zdb_id='ZDB-TGCONSTRCT-071017-1' and cc_order=4;
delete from construct_component where cc_construct_Zdb_id='ZDB-TGCONSTRCT-071017-1' and cc_order=5;
delete from construct_component where cc_construct_Zdb_id='ZDB-TGCONSTRCT-071017-1' and cc_order=6;


insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-071017-1','text component','prefix component','PAC',1,2);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-071017-1','controlled vocab component','construct wrapper component','ZDB-CV-150506-7','(',1,3);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-071017-1','promoter of','promoter component','ZDB-GENE-991110-14','myb',1,4);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-071017-1','controlled vocab component','promoter component','ZDB-CV-150506-10',':',1,5);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-071017-1','text component','coding component','','2x',1,6);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-071017-1','coding sequence of','coding component','ZDB-GENE-991110-14','myb',1,7);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-071017-1','coding sequence of','coding component','','-',1,8);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-071017-1','coding sequence of','coding component','ZDB-EFG-070117-1','EGFP',1,9);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-071017-1','controlled vocab component','construct wrapper component','ZDB-CV-150506-8',')',1,10);


create temp table tmp_cmrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50)) with no log;


insert into tmp_cmrel(relid,consid,mrkrid,reltype) values (get_id('CMREL'),'ZDB-TGCONSTRCT-071017-1','ZDB-GENE-991110-14','coding sequence of');

insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_cmrel;

insert into construct_marker_relationship(conmrkrrel_zdb_id,conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type) select relid,consid,mrkrid,reltype from tmp_cmrel;

drop table tmp_cmrel;

create temp table tmp_mrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50)) with no log;

insert into tmp_mrel(relid,consid,mrkrid,reltype) values (get_id('MREL'),'ZDB-TGCONSTRCT-071017-1','ZDB-GENE-991110-14','coding sequence of');

insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_mrel;

insert into marker_relationship(mrel_zdb_id,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) select relid,consid,mrkrid,reltype from tmp_mrel;

insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id,recattrib_source_type) select relid,'ZDB-PUB-070629-5','standard' from tmp_mrel;

drop table tmp_mrel;