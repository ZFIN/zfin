--liquibase formatted sql
--changeset pm:CUR-987



update construct set construct_name='Tg(5xUAS:eNpHR3-YFP,myl7:EGFP)' where construct_zdb_id='ZDB-TGCONSTRCT-200722-2';
update marker set mrkr_name='Tg(5xUAS:eNpHR3-YFP,myl7:EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-200722-2';
update marker set mrkr_abbrev='Tg(5xUAS:eNpHR3-YFP,myl7:EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-200722-2';


update construct_component set cc_component=','  where cc_component_zdb_id='ZDB-CV-150506-8' and cc_construct_zdb_id='ZDB-TGCONSTRCT-200722-2';
update construct_component set cc_component_category='cassette delimiter'  where cc_component_zdb_id='ZDB-CV-150506-8' and cc_construct_zdb_id='ZDB-TGCONSTRCT-200722-2';
update construct_component set cc_component_zdb_id='ZDB-CV-150506-11'  where cc_component_zdb_id='ZDB-CV-150506-8' and cc_construct_zdb_id='ZDB-TGCONSTRCT-200722-2';
update construct_component set cc_component_type='controlled vocab component'  where cc_component_category='cassette delimiter' and cc_construct_zdb_id='ZDB-TGCONSTRCT-200722-2';


insert into construct_component(cc_construct_zdb_id, cc_component,cc_component_zdb_id,cc_order,cc_cassette_number,cc_component_category,cc_component_type) values('ZDB-TGCONSTRCT-200722-2','myl7','ZDB-GENE-991019-3',12,2,'promoter component','promoter of');
insert into construct_component(cc_construct_zdb_id, cc_component,cc_component_zdb_id,cc_order,cc_cassette_number,cc_component_category,cc_component_type) values('ZDB-TGCONSTRCT-200722-2',':','ZDB-CV-150506-10',13,2,'promoter component','controlled vocab component');
insert into construct_component(cc_construct_zdb_id, cc_component,cc_component_zdb_id,cc_order,cc_cassette_number,cc_component_category,cc_component_type) values('ZDB-TGCONSTRCT-200722-2','EGFP','ZDB-EFG-070117-1',14,2,'coding component','ccoding sequence of');
insert into construct_component(cc_construct_zdb_id, cc_component,cc_component_zdb_id,cc_order,cc_cassette_number,cc_component_category,cc_component_type) values('ZDB-TGCONSTRCT-200722-2',')','ZDB-CV-150506-10',15,2,'construct wrapper component','controlled vocab component');


create temp table tmp_cmrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_cmrel (relid, consid ,mrkrid,reltype) values ('ZDB-TGCONSTRCT-200722-2','ZDB-TGCONSTRCT-200722-2','ZDB-EFG-070117-1','coding sequence of');
insert into tmp_cmrel (relid, consid ,mrkrid,reltype) values ('ZDB-TGCONSTRCT-200722-2','ZDB-TGCONSTRCT-200722-2','ZDB-GENE-991019-3','promoter of');
update tmp_cmrel set relid=get_id('CMREL');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_cmrel;
insert into construct_marker_relationship(conmrkrrel_zdb_id,conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type) select relid,consid,mrkrid,reltype from tmp_cmrel;
drop table tmp_cmrel;

create temp table tmp_mrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_mrel (relid, consid ,mrkrid,reltype) values ('ZDB-TGCONSTRCT-200722-2','ZDB-TGCONSTRCT-200722-2','ZDB-EFG-070117-1','coding sequence of');
insert into tmp_mrel (relid, consid ,mrkrid,reltype) values ('ZDB-TGCONSTRCT-200722-2','ZDB-TGCONSTRCT-200722-2','ZDB-GENE-991019-3','promoter of');
update tmp_mrel set relid=get_id('MREL');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_mrel;
insert into marker_relationship(mrel_zdb_id,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) select relid,consid,mrkrid,reltype from tmp_mrel;
drop table tmp_mrel;













