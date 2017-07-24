--liquibase formatted sql
--changeset prita:ZFIN-5671

update construct set construct_name='Tg(UAS:Hsa.JAG1,myl7:EGFP)' where construct_zdb_id='ZDB-TGCONSTRCT-100726-2';
update marker set mrkr_name='Tg(UAS:Hsa.JAG1,myl7:EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-100726-2';
update marker set mrkr_abbrev='Tg(UAS:Hsa.JAG1,myl7:EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-100726-2';

delete from construct_component where cc_construct_Zdb_id='ZDB-TGCONSTRCT-100726-2' and cc_order=8;

insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-100726-2','controlled vocab component','promoter component','ZDB-CV-150506-11',',',2,8);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-100726-2','promoter of','promoter component','ZDB-GENE-991019-3','myl7',2,9);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-100726-2','controlled vocab component','promoter component','ZDB-CV-150506-10',':',2,10);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-100726-2','coding sequence of','coding component','ZDB-EFG-070117-1','EGFP',2,11);



insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-100726-2','controlled vocab component','construct wrapper component','ZDB-CV-150506-8',')',2,12);

create temp table tmp_cmrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50)) with no log;

insert into tmp_cmrel(relid,consid,mrkrid,reltype) values (get_id('CMREL'),'ZDB-TGCONSTRCT-100726-2','ZDB-GENE-991019-3','promoter of');
insert into tmp_cmrel(relid,consid,mrkrid,reltype) values (get_id('CMREL'),'ZDB-TGCONSTRCT-100726-2','ZDB-EFG-070117-1','coding sequence of');

insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_cmrel;

insert into construct_marker_relationship(conmrkrrel_zdb_id,conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type) select relid,consid,mrkrid,reltype from tmp_cmrel;

drop table tmp_cmrel;

create temp table tmp_mrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50)) with no log;

insert into tmp_mrel(relid,consid,mrkrid,reltype) values (get_id('MREL'),'ZDB-TGCONSTRCT-100726-2','ZDB-GENE-991019-3','promoter of');
insert into tmp_mrel(relid,consid,mrkrid,reltype) values (get_id('MREL'),'ZDB-TGCONSTRCT-100726-2','ZDB-EFG-070117-1','coding sequence of');

insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_mrel;

insert into marker_relationship(mrel_zdb_id,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) select relid,consid,mrkrid,reltype from tmp_mrel;

insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id,recattrib_source_type) select relid,'ZDB-PUB-100504-15','standard' from tmp_mrel;

drop table tmp_mrel;